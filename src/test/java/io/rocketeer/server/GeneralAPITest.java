package io.rocketeer.server;

import io.rocketeer.ClientConfiguration;
import io.rocketeer.ClientContainer;
import io.rocketeer.ClientFactory;
import io.rocketeer.ServerConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class GeneralAPITest {

    private static int port = 8999;
    private static WebSocketServer server;

    @BeforeClass
    public static void setup() throws Exception {
        server = new WebSocketServer(port);
        server.registerServer(
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

        final ClientContainer client = ClientFactory.createClient();
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
