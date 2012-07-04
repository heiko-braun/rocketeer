package io.rocketeer;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface RemoteEndpoint {
    void sendString(java.lang.String text);
    void sendBytes(byte[] data);
}
