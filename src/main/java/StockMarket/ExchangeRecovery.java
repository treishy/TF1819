package StockMarket;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.util.*;
import java.util.stream.Collectors;

class StateRequest {
    String target;
    String sender;

    public StateRequest ( String sender, String target ) {
        this.sender = sender;
        this.target = target;
    }

    public String getSender () {
        return sender;
    }

    public String getTarget () {
        return this.target;
    }
}

class StateResponse {
    int index;
    int total;
    byte[] state;

    public StateResponse ( int index, int total, byte[] state ) {
        this.index = index;
        this.total = total;
        this.state = state;
    }

    public int getIndex () {
        return this.index;
    }

    public int getTotal () {
        return this.total;
    }

    public byte[] getState () {
        return this.state;
    }
}

interface Stateful<T> {
    SpreadConnection getConnection ();

    SpreadGroup getPublicGroup ();

    Serializer getSerializer ();

    T getState ();

    void setState ( T state );
}

public class ExchangeRecovery<T> {
    private Stateful<T> stateful;

    private boolean isRecovered = false;

    private boolean isRecovering = false;

    private boolean isBuffering = false;

    private Queue<SpreadMessage> queue = new LinkedList<SpreadMessage>();

    private List<StateResponse> stateResponses = new ArrayList<>();

    private int maxMessageSize = 90000;

    public ExchangeRecovery ( Stateful<T> stateful ) {
        this.stateful = stateful;
    }

    public Queue<SpreadMessage> getQueue () {
        return this.queue;
    }

    public boolean getIsRecovered () {
        return this.isRecovered;
    }

    public void setIsRecovered ( boolean isRecovered ) {
        this.isRecovered = isRecovered;
    }

    public List<byte[]> chunkBytes ( byte[] source, int chunksize ) {
        List<byte[]> result = new ArrayList<>();

        int start = 0;

        while ( start < source.length ) {
            int end = Math.min( source.length, start + chunksize );

            result.add( Arrays.copyOfRange( source, start, end ) );

            start += chunksize;
        }

        return result;
    }

    public byte[] mergeChunks ( List<byte[]> chunks ) {
        int sum = chunks.stream().mapToInt( bytes -> bytes.length ).sum();

        byte[] merged = new byte[ sum ];

        int cursor = 0;

        for ( byte[] chunk : chunks ) {
            System.arraycopy( chunk, 0, merged, cursor, cursor + chunk.length );

            cursor += chunk.length;
        }

        return merged;
    }

    /**
     * This method will be called when a server is restarted, and will choose one member of the current group to ask
     * for a state transfer.
     *
     * @param members
     * @throws SpreadException
     */
    public void requestState ( SpreadGroup[] members ) throws SpreadException {
        this.isRecovering = true;

        int otherIndex = 0;

        for ( int i = 0 ; i < members.length ; i++ ) {
            if ( !members[ i ].toString().equals( this.stateful.getConnection().getPrivateGroup().toString() ) ) {
                otherIndex = i;
                break;
            }
        }

        StateRequest request = new StateRequest( this.stateful.getConnection().getPrivateGroup().toString(), members[ otherIndex ].toString() );

        SpreadMessage message = new SpreadMessage();

        byte[] bytes = this.stateful.getSerializer().encode( request );

        message.setSelfDiscard( false );
        message.addGroup( this.stateful.getPublicGroup() );
        message.setData( bytes );

        this.stateful.getConnection().multicast( message );
    }

    /**
     * This method is called when a message request is received.
     *
     * @param target
     * @param state
     * @throws SpreadException
     */
    public void sendState ( SpreadGroup target, T state ) throws SpreadException {
        List<byte[]> bytes = this.chunkBytes( this.stateful.getSerializer().encode( state ), maxMessageSize );

        for ( int i = 0 ; i < bytes.size() ; i++ ) {
            SpreadMessage message = new SpreadMessage();

            message.addGroup( target );
            message.setData( this.stateful.getSerializer().encode( new StateResponse( i, bytes.size(), bytes.get( i ) ) ) );

            this.stateful.getConnection().multicast( message );
        }
    }

    public boolean onMembershipMiddleware ( SpreadMessage message ) throws SpreadException {
        if ( message.isMembership() && !this.isRecovered && !this.isRecovering ) {
            SpreadGroup[] members = message.getMembershipInfo().getMembers();

            if ( members.length <= 1 ) {
                this.setIsRecovered( true );
            } else {
                this.requestState( members );
            }

            return true;
        }

        return false;
    }

    public boolean onStateRequestMiddleware ( SpreadMessage message, Object object ) throws SpreadException {
        if ( object instanceof StateRequest ) {
            StateRequest request = ( StateRequest ) object;

            // When we receive our own state request, we should start buffering all other operation messages
            if ( request.getSender().equals( this.stateful.getConnection().getPrivateGroup().toString() ) ) {
                this.isBuffering = true;
            } else if ( request.getTarget().equals( this.stateful.getConnection().getPrivateGroup().toString() ) ) {
                this.sendState( message.getSender(), this.stateful.getState() );
            }

            return true;
        }

        return false;
    }

    public boolean onStateResponseMiddleware ( SpreadMessage message, Object object ) {
        if ( object instanceof StateResponse ) {
            StateResponse response = ( StateResponse ) object;

            this.stateResponses.add( response );

            if ( this.stateResponses.size() == response.total ) {
                List<byte[]> chunks = this.stateResponses
                        .stream()
                        .sorted( Comparator.comparingInt( StateResponse::getIndex ) )
                        .map( StateResponse::getState )
                        .collect( Collectors.toList() );

                byte[] bytes = this.mergeChunks( chunks );

                T state = this.stateful.getSerializer().decode( bytes );

                this.stateful.setState( state );

                this.isBuffering = false;

                this.isRecovered = true;

                this.isRecovering = false;
            }

            return true;
        }

        return false;
    }

    public boolean onBufferedMessageMiddleware ( SpreadMessage message ) {
        if ( this.isBuffering ) {
            this.queue.add( message );

            return true;
        }

        return false;
    }
}
