package io.rocketeer.server;

import io.rocketeer.Rocketeer;
import io.rocketeer.ServerConfiguration;
import io.rocketeer.ServerContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.projectodd.stilts.stomp.StompMessages;
import org.projectodd.stilts.stomp.Subscription;
import org.projectodd.stilts.stomp.client.ClientSubscription;
import org.projectodd.stilts.stomp.client.ClientTransaction;
import org.projectodd.stilts.stomp.client.StompClient;
import org.projectodd.stilts.stomp.client.helpers.MessageAccumulator;

import java.net.URI;

import static org.junit.Assert.assertTrue;

/**
 * @author Heiko Braun
 * @date 6/29/12
 */
public class StompProtocolTest {

    private static int port = 8999;
    private static ServerContainer server;

    @BeforeClass
    public static void setup() throws Exception {
        server = Rocketeer.createServer(port);
        server.registerEndpoint(
                new StompEndpoint(),
                new ServerConfiguration(new URI("/stomp"))
        );
        server.start();
    }

    @AfterClass
    public static void teardown()
    {
        server.stop();
    }

    @Test
    public void connectionTest() throws Exception {
        StompClient client = new StompClient( "stomp+ws://localhost:"+port+"/stomp" );
        client.connect();

        /*final MessageAccumulator messageHandler = new MessageAccumulator();
        ClientSubscription subscription1 =
                client.subscribe( "/queues/foo" )
                        .withMessageHandler(messageHandler)
                        .withAckMode(Subscription.AckMode.CLIENT_INDIVIDUAL)
                        .start();

        ClientSubscription subscription2 =
                client.subscribe( "/queues/foo" )
                        .withMessageHandler(messageHandler)
                        .withAckMode(Subscription.AckMode.AUTO)
                        .start();

        ClientTransaction tx = client.begin();

        for (int i = 0; i < 10; ++i) {
            tx.send(
                    StompMessages.createStompMessage("/queues/foo", "msg-" + i)
            );
        }

        tx.commit();

        subscription1.unsubscribe();
        subscription2.unsubscribe();             */


        client.disconnect();

        Thread.sleep(1000);
        assertTrue(client.isDisconnected());
    }
}
