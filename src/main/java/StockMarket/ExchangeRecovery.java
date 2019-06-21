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

    Serializer getSerializer ();

    T getState ();

    void setState ( T state );
}

public class ExchangeRecovery<T> {
    private Stateful<T> stateful;

    private boolean isRecovered = false;

    private boolean isBuffering = true;

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
        StateRequest request = new StateRequest( this.stateful.getConnection().getPrivateGroup().toString(), members[ 0 ].toString() );

        SpreadMessage message = new SpreadMessage();

        byte[] bytes = this.stateful.getSerializer().encode( request );

        message.setSelfDiscard( false );
        message.addGroup( members[ 0 ] );
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

    public boolean onStateRequestMiddleware ( SpreadMessage message, Object object ) throws SpreadException {
        if ( object instanceof StateRequest ) {
            StateRequest request = ( StateRequest ) object;

            // When we receive our own state request, we should start buffering all other operation messages
            // TODO check our sender "comparison" algorithm
            if ( request.getSender().equals( message.getSender().toString() ) ) {
                this.isBuffering = true;
            } else if ( request.getTarget().equals( message.getSender().toString() ) ) {
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

    public static void main ( String[] args ) {
        try {
            SpreadConnection connection = new SpreadConnection();

            SpreadGroup publicGroup = new SpreadGroup();

            Serializer serializer = new SerializerBuilder().addType( StateRequest.class ).addType( StateResponse.class ).build();

            // TODO instead of null we will have to pass a real object for this to work
            ExchangeRecovery<Integer> recovery = new ExchangeRecovery<>( null );

            connection.add( message -> {
                try {
                    Object obj = serializer.decode( message.getData() );

                    if ( recovery.onStateRequestMiddleware( message, obj ) ) return;

                    if ( recovery.onStateResponseMiddleware( message, obj ) ) return;

                    if ( recovery.onBufferedMessageMiddleware( message ) ) return;

                    if ( recovery.getIsRecovered() ) {
                        SpreadMessage buffered = null;

                        while ( ( buffered = recovery.getQueue().poll() ) != null ) {
                            // Execute operation buffered
                        }

                        // Execute operation message
                    }
                } catch ( SpreadException e ) {
                    e.printStackTrace();
                }
            } );


            connection.connect( null, 4803, "servers" + args[ 0 ], false, true );

            publicGroup.join( connection, "servers" );
        } catch ( SpreadException e ) {
            e.printStackTrace();
        }
    }
}
