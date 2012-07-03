package io.rocketeer.autobahn;

import io.rocketeer.Endpoint;
import io.rocketeer.MessageListener;
import io.rocketeer.Rocketeer;
import io.rocketeer.ServerConfiguration;
import io.rocketeer.ServerContainer;
import io.rocketeer.Session;

import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.net.URI;


/**
 * @author Heiko Braun
 * @date 7/3/12
 */
public class TestServer {
    public static void main(String[] args) throws Exception
    {

        if(args.length == 0)
            throw new IllegalArgumentException("Port number argument is missing");

        ServerContainer server = Rocketeer.createServer(Integer.valueOf(args[0]));
        server.registerEndpoint(
                new Endpoint() {
                    @Override
                    public void hasOpened(final Session session) {
                        session.addMessageListener(new MessageListener.Text() {
                            public void onMessage(String text) {

                                // response
                                session.getRemote().sendString(text);

                            }
                        });
                    }
                },
                new ServerConfiguration(new URI("/echo"))
        );
        server.start();
    }
}
