package io.rocketeer;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface ClientContainer {
    void connect(Endpoint endpoint, ClientConfiguration olc);
}
