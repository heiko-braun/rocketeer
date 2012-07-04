package io.rocketeer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public abstract class Endpoint {

    private final static Logger log = LoggerFactory.getLogger(Endpoint.class);

    public abstract void hasOpened(Session session);

    public void hasClosed(Session session) {
        log.debug("Session closed.");
    }

    public void handleError(Throwable e, Session s) {
        log.error("Unknown error", e);
    }

}
