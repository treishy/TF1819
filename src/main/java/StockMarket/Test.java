package StockMarket;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.ArrayList;
import java.util.List;

class FakeState {
    long counterid;
    ExchangeImpl exchange;

    public FakeState(long counterid, ExchangeImpl exchange) {
        this.counterid = counterid;
        this.exchange = exchange;
    }
}

public class Test {

    public static void main(String args[]) {

        Serializer serializer = new SerializerBuilder()
                //.withTypes(ExchangeImpl.class, Value.class)
                .addType(FakeState.class)
                .addType(ExchangeImpl.class)
                .addType(Value.class)
                .build();

        List<Value> values = new ArrayList<>();
        values.add(new Value(0, "TF", "TF", "TF", 100));
        ExchangeImpl exchange = new ExchangeImpl(values);
        FakeState state = new FakeState(0, exchange);

        //Value i = new Value(0, "TF", "TF", "TF", 100);
        //List<Value> list = new ArrayList<>();
        //list.add(i);

        byte[] bytes = serializer.encode(state);
        System.out.println("Encoding ...");


        FakeState state1 = serializer.decode(bytes);
        System.out.println("Decoding ...");


        Value i2 = state1.exchange.getCatalogValues().get(0);
        System.out.println(i2.getName());
    }
}
