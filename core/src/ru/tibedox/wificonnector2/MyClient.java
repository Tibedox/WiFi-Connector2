package ru.tibedox.wificonnector2;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;

public class MyClient {
    Client client;

    boolean isCantConnected;
    private final MyRequest request;
    private MyResponse response;

    public MyClient(String ipServer, MyRequest request) {
        this.request = request;
        response = new MyResponse();

        client = new Client();
        client.start();
        try {
            client.connect(5000, ipServer, 54555, 54777);
        } catch (IOException e) {
            isCantConnected = true;
            e.printStackTrace();
        }

        Kryo kryoClient = client.getKryo();
        kryoClient.register(MyRequest.class);
        kryoClient.register(MyResponse.class);

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof MyResponse) {
                    response = (MyResponse) object;
                }
            }
        });
    }

    public MyResponse getResponse() {
        return response;
    }

    void send() {
        client.sendTCP(request);
    }
}
