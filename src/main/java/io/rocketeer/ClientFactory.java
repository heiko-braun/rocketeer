package io.rocketeer;

import io.rocketeer.client.ClientContainerImpl;

/**
 * @author Heiko Braun
 * @date 6/27/12
 */
public class ClientFactory {

    public static ClientContainer createClient() {
        return new ClientContainerImpl();
    }
}
