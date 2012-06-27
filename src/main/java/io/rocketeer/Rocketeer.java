package io.rocketeer;

import io.rocketeer.client.ClientContainerImpl;
import io.rocketeer.server.WebSocketServer;

/**
 * @author Heiko Braun
 * @date 6/27/12
 */
public class Rocketeer {

    public static ServerContainer createServer(int port)
    {
        return new WebSocketServer(port);
    }

    public static ClientContainer createClient() {
        return new ClientContainerImpl();
    }
}
