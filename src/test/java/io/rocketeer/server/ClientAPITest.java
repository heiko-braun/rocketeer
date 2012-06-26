package io.rocketeer.server;

import io.rocketeer.client.WebSocketCallback;
import io.rocketeer.client.WebSocketClient;
import io.rocketeer.client.WebSocketClientFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class ClientAPITest {
    private static int port = 8999;
    private static WebSocketServer server;

    @BeforeClass
    public static void setup() {
        server = new WebSocketServer(port);
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
//        Thread.sleep(1000);

  //      assertTrue(callback.connected);
        client.send(TestCallback.TEST_MESSAGE);
    //    assertEquals(TestCallback.TEST_MESSAGE, callback.messageReceived);
        client.disconnect();
      //  Thread.sleep(1000);

        //assertFalse(callback.connected);
    }

    class TestCallback implements WebSocketCallback {

        public static final String TEST_MESSAGE = "Testing this WebSocket";
        public boolean connected = false;
        public String messageReceived = null;

        public void onConnect(ChannelHandlerContext context) {
            System.out.println("WebSocket connected "+context.getChannel().getRemoteAddress());
            connected = true;
        }

        public void onDisconnect(ChannelHandlerContext client) {

        }

        public void onMessage(WebSocketClient client, WebSocketFrame frame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame)frame;
            System.out.println("Message:" + textFrame.getText());
            messageReceived = textFrame.getText();
        }

        public void onError(Throwable t) {
            t.printStackTrace();
        }
    }
}
