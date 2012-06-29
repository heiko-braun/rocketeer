package io.rocketeer.server;

import io.rocketeer.ClientConfiguration;
import io.rocketeer.ClientContainer;
import io.rocketeer.Endpoint;
import io.rocketeer.MessageListener;
import io.rocketeer.Rocketeer;
import io.rocketeer.ServerConfiguration;
import io.rocketeer.ServerContainer;
import io.rocketeer.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

/**
 * Demonstration of the current high level API for both client and server.<br/>
 * Does lean on <a href="http://jcp.org/en/jsr/detail?id=356">JSR 356</a>
 *
 * @author Heiko Braun
 */
public class APIExamplesTest {

    private static int port = 8999;
    private static ServerContainer server;
    private static boolean clientTerminates = true;

    @BeforeClass
    public static void setup() throws Exception {
        server = Rocketeer.createServer(port);
        server.registerEndpoint(
                new Endpoint() {
                    @Override
                    public void hasOpened(final Session session) {
                        session.addMessageListener(new MessageListener.Text() {
                            public void onMessage(String text) {

                                // response
                                session.getRemote().sendString(text);
                                if(!clientTerminates())
                                    session.close();

                            }
                        });
                    }
                },
                new ServerConfiguration(new URI("/echo"))
        );
        server.start();
    }

    private static boolean clientTerminates() {
        return clientTerminates;
    }

    @AfterClass
    public static void teardown()
    {
        server.stop();
    }

    @Test
    public void testMessageSend() throws Exception {

        final ClientContainer client = Rocketeer.createClient();

        EchoClient endpoint = new EchoClient();
        client.connect(
                endpoint,
                new ClientConfiguration(new URI("ws://localhost:" + port + "/echo"))
        );

        Thread.sleep(1000);
        assertTrue(endpoint.isConnected());
        assertEquals(EchoClient.TEST_MESSAGE, endpoint.getMessageReceived());

        if(clientTerminates())
        {
            endpoint.disconnect();
            Thread.sleep(1000);
            assertFalse(endpoint.isConnected());
        }
    }

    @Test
    public void testServerTerminates() throws Exception {

        clientTerminates=false;

        final ClientContainer client = Rocketeer.createClient();

        EchoClient endpoint = new EchoClient();
        client.connect(
                endpoint,
                new ClientConfiguration(new URI("ws://localhost:" + port + "/echo"))
        );

        Thread.sleep(1000);
        assertFalse(endpoint.isConnected());

    }
}
