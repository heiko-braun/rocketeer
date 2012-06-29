package io.rocketeer.server;

import io.rocketeer.Endpoint;
import io.rocketeer.MessageListener;
import io.rocketeer.Session;

/**
 * @author Heiko Braun
 * @date 6/29/12
 */
public class StompEndpoint extends Endpoint {

    @Override
    public void hasOpened(final Session session) {
        session.addMessageListener(new MessageListener.Text() {
            public void onMessage(String text) {

                System.out.println("Received "+text);
                session.close();
            }
        });
    }
}
