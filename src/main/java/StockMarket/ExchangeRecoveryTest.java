package StockMarket;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

public class ExchangeRecoveryTest implements Stateful<Integer> {
    protected int id;

    protected SpreadConnection connection;

    protected SpreadGroup publicGroup;

    protected Serializer serializer;

    protected int state;

    protected ExchangeRecovery<Integer> recovery;

    public ExchangeRecoveryTest ( int id, int state ) {
        this.id = id;
        this.state = state;

        this.connection = new SpreadConnection();

        this.publicGroup = new SpreadGroup();

        this.serializer = new SerializerBuilder().addType( StateRequest.class ).addType( StateResponse.class ).build();

        this.recovery = new ExchangeRecovery<>( this );

        connection.add( message -> {
            try {
                if ( recovery.onMembershipMiddleware( message ) ) return;

                if (message.isRegular()) {
                    Object obj = serializer.decode( message.getData() );

                    if ( recovery.onStateRequestMiddleware( message, obj ) ) return;

                    if ( recovery.onStateResponseMiddleware( message, obj ) ) return;

                    if ( recovery.onBufferedMessageMiddleware( message ) ) return;

                    if ( recovery.getIsRecovered() ) {
                        SpreadMessage buffered = null;

                        while ( ( buffered = recovery.getQueue().poll() ) != null ) {
                            // Execute operation buffered
                        }

                        recovery.getQueue().clear();

                        // Execute operation message
                    }
                }
            } catch ( SpreadException e ) {
                e.printStackTrace();
            }
        } );
    }

    @Override
    public SpreadConnection getConnection () {
        return connection;
    }

    @Override
    public SpreadGroup getPublicGroup () {
        return this.publicGroup;
    }

    @Override
    public Serializer getSerializer () {
        return serializer;
    }

    @Override
    public Integer getState () {
        System.out.printf("Server %d getting state %d\n", this.id, this.state);
        return this.state;
    }

    @Override
    public void setState ( Integer state ) {
        System.out.printf("Server %d setting state to %d\n", this.id, state);
        this.state = state;
    }

    public void start () throws SpreadException {
        connection.connect( null, 4803, "servers-" + id, false, true );

        publicGroup.join( connection, "servers" );
    }

    public static void main ( String[] args ) {
        try {
            ExchangeRecoveryTest client1 = new ExchangeRecoveryTest( 1, 10 );

            client1.start();

            ExchangeRecoveryTest client2 = new ExchangeRecoveryTest( 2, 0 );

            client2.start();

            System.in.read();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }
}
