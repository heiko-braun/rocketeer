package io.rocketeer.server;

import io.rocketeer.Endpoint;
import io.rocketeer.Session;

import java.util.List;
import java.util.Map;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface EndpointSessions<T extends Session> {

    Map<String, Endpoint> getEndpoints();

    List<T> getSessions();
}
