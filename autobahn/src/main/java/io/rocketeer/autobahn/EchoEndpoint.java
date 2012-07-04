package io.rocketeer.autobahn;

import io.rocketeer.Endpoint;
import io.rocketeer.MessageListener;
import io.rocketeer.Session;

/**
 * @author Heiko Braun
 * @date 7/3/12
 */
public class EchoEndpoint extends Endpoint {
    @Override
    public void hasOpened(final Session session) {
        session.addMessageListener(new MessageListener.Text() {
            public void onMessage(String text) {
                session.getRemote().sendString(text);
            }
        });

        session.addMessageListener(new MessageListener.Binary() {
            public void onMessage(byte[] data) {
                session.getRemote().sendBytes(data);
            }
        });
    }
}
