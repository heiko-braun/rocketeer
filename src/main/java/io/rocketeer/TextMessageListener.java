package io.rocketeer;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface TextMessageListener extends MessageListener {
    void onMessage(java.lang.String text);
}
