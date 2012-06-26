package io.rocketeer;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface Session {

    void addMessageListener(MessageListener listener);
    RemoteEndpoint getRemote();
    void close();

    String getProtocolVersion();
    String getSubprotocol();

    long getTimeout();
    void setTimeout(long seconds);
}
