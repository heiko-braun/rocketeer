package io.rocketeer;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface Session {

    void addMessageListener(MessageListener listener);
    RemoteEndpoint getRemote();
    void close() throws java.io.IOException;
}
