package io.rocketeer;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public abstract class Endpoint {

    public abstract void hasOpened(Session session);

    public void hasClosed(Session session) {

    }

    public void handleError(java.lang.Exception e, Session s) {

    }

}
