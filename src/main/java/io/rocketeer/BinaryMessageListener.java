package io.rocketeer;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface BinaryMessageListener extends MessageListener{
    void onMessage(byte[] data);
}
