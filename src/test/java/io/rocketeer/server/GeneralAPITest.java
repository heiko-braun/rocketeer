package io.rocketeer.server;

import io.rocketeer.ClientConfiguration;
import io.rocketeer.ClientContainer;
import io.rocketeer.ClientFactory;
import io.rocketeer.ServerConfiguration;
import io.rocketeer.client.WebSocketCallback;
import io.rocketeer.client.WebSocketClient;
import io.rocketeer.client.WebSocketClientFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
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

        /*WebSocketClientFactory clientFactory = new WebSocketClientFactory();
        final TestCallback callback = new TestCallback();

        WebSocketClient client = clientFactory.newClient(
                new URI("ws://localhost:" + port + "/websocket"),
                callback
        );

        client.connect().awaitUninterruptibly();

        client.send(TestCallback.TEST_MESSAGE);
        Thread.sleep(1000);
        assertEquals(TestCallback.TEST_MESSAGE, callback.messageReceived);
        client.disconnect();
        Thread.sleep(500);
        assertFalse(callback.connected);*/

        final ClientContainer client = ClientFactory.createClient();
        EchoClient endpoint = new EchoClient();
        client.connect(
                endpoint,
                new ClientConfiguration(new URI("ws://localhost:" + port + "/websocket"))
        );

        Thread.sleep(3000);
        assertTrue(endpoint.isConnected());
        assertEquals(EchoClient.TEST_MESSAGE, endpoint.getMessageReceived());


    }
}
