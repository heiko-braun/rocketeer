package io.rocketeer.server;

import io.rocketeer.ServerConfiguration;
import io.rocketeer.client.WebSocketCallback;
import io.rocketeer.client.WebSocketClient;
import io.rocketeer.client.WebSocketClientFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class ClientAPITest {
    private static int port = 8999;
    private static WebSocketServer server;

    @BeforeClass
    public static void setup() throws Exception {
        server = new WebSocketServer(port);
        server.registerServer(new EchoEndpoint(), new ServerConfiguration(new URI("/websocket")));
        server.start();
    }

    @AfterClass
    public static void teardown()
    {
        server.stop();
    }

    @Test
    public void webSocketClient() throws Exception {

        WebSocketClientFactory clientFactory = new WebSocketClientFactory();
        final TestCallback callback = new TestCallback();

        WebSocketClient client = clientFactory.newClient(
                new URI("ws://localhost:" + port + "/websocket"),
                callback
        );

        client.connect().awaitUninterruptibly();
        Thread.sleep(3000);

        //assertTrue(callback.connected);
        client.send(TestCallback.TEST_MESSAGE);
        Thread.sleep(3000);
        //    assertEquals(TestCallback.TEST_MESSAGE, callback.messageReceived);
        client.disconnect();
        //assertFalse(callback.connected);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidWebContext() throws Exception {

        WebSocketClientFactory clientFactory = new WebSocketClientFactory();
        final TestCallback callback = new TestCallback();

        WebSocketClient client = clientFactory.newClient(
                new URI("ws://localhost:" + port + "/invalidContext"),
                callback
        );

        final ChannelFuture connection= client.connect();
        connection.awaitUninterruptibly();
        Thread.sleep(3000);

        if(callback.failedWith!=null)
        {
            System.out.println("Failed with: "+callback.failedWith.getMessage());
            throw new RuntimeException(callback.failedWith.getMessage());
        }

        client.disconnect();
    }

    class TestCallback implements WebSocketCallback {

        public static final String TEST_MESSAGE = "Testing this WebSocket";
        public boolean connected = false;
        public String messageReceived = null;
        public Throwable failedWith = null;

        public void onConnect(ChannelHandlerContext context) {
            System.out.println("Client connected: "+context.getChannel().getId());
            connected = true;
        }

        public void onDisconnect(ChannelHandlerContext client) {
           connected = false;
        }

        public void onMessage(WebSocketClient client, WebSocketFrame frame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame)frame;
            System.out.println("Client recv: " + textFrame.getText());
            messageReceived = textFrame.getText();
        }

        public void onError(Throwable t) {
            failedWith = t;
        }
    }
}
