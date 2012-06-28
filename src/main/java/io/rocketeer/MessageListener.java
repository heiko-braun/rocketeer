package io.rocketeer;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface MessageListener {

    interface Text extends MessageListener {
        void onMessage(String text);
    }

    interface Binary extends MessageListener {
        void onMessage(byte[] data);
    }
}
