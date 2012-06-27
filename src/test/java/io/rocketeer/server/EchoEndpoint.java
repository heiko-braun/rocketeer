package io.rocketeer.server;

import io.rocketeer.Endpoint;
import io.rocketeer.Session;
import io.rocketeer.TextMessageListener;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public class EchoEndpoint extends Endpoint {
    @Override
    public void hasOpened(final Session session) {

        session.addMessageListener(new TextMessageListener() {
            public void onMessage(String text) {

                // response
                session.getRemote().sendString(text);
            }
        });
    }
}
