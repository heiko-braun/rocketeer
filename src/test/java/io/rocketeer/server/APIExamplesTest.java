package io.rocketeer.server;

import io.rocketeer.ClientConfiguration;
import io.rocketeer.ClientContainer;
import io.rocketeer.Rocketeer;
import io.rocketeer.ServerConfiguration;
import io.rocketeer.ServerContainer;
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

    @BeforeClass
    public static void setup() throws Exception {
        server = Rocketeer.createServer(port);
        server.registerEndpoint(
                new EchoEndpoint(),
                new ServerConfiguration(new URI("/websocket"))
        );
        server.start();
    }

    @AfterClass
    public static void teardown()
    {
        server.stop();
    }

    @Test
    public void webSocketClient() throws Exception {

        final ClientContainer client = Rocketeer.createClient();

        EchoClient endpoint = new EchoClient();
        client.connect(
                endpoint,
                new ClientConfiguration(new URI("ws://localhost:" + port + "/websocket"))
        );

        Thread.sleep(1000);
        assertTrue(endpoint.isConnected());
        assertEquals(EchoClient.TEST_MESSAGE, endpoint.getMessageReceived());

        endpoint.disconnect();
        Thread.sleep(1000);
        assertFalse(endpoint.isConnected());
    }
}
