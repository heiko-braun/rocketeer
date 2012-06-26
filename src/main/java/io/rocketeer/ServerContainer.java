package io.rocketeer;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface ServerContainer extends ClientContainer {
    void registerServer(Endpoint endpoint, ServerConfiguration ilc);
}
