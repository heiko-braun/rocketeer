package io.rocketeer.server;

import io.rocketeer.Endpoint;
import io.rocketeer.Session;
import io.rocketeer.TextMessageListener;

/**
 * @author Heiko Braun
 * @date 6/27/12
 */
public class EchoClient extends Endpoint {

    public static final String TEST_MESSAGE = "Testing this WebSocket";
    private boolean connected = false;
    private String messageReceived = null;

    @Override
    public void hasOpened(Session session) {
        connected = true;

        session.addMessageListener(new TextMessageListener() {
            public void onMessage(String text) {
                System.out.println("Client recv: " + text);
                messageReceived = text;
            }
        });

        session.getRemote().sendString(TEST_MESSAGE);
    }

    @Override
    public void hasClosed(Session session) {
        connected = false;
        System.out.println("Client disconnected");
    }

    public boolean isConnected() {
        return connected;
    }

    public String getMessageReceived() {
        return messageReceived;
    }
}
