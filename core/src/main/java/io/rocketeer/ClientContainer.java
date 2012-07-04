package io.rocketeer;

import java.util.List;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface ClientContainer {
    void connect(Endpoint endpoint, ClientConfiguration config);

    List<? extends Session> getActiveSessions();


}
