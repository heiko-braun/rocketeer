package io.rocketeer;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface MessageListener {

    static interface Text extends MessageListener {
        void onMessage(String text);
    }

    static interface Binary extends MessageListener {
        void onMessage(byte[] data);
    }
}
