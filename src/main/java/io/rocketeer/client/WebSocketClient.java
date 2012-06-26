package io.rocketeer.client;

import org.jboss.netty.channel.ChannelFuture;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface WebSocketClient {

    /**
     * Connect to server
     * Host and port is setup by the factory.
     *
     * @return Connect future. Fires when connected.
     */
    public ChannelFuture connect();

    /**
     * Disconnect from the server
     * @return Disconnect future. Fires when disconnected.
     */
    public ChannelFuture disconnect();

    /**
     * Send data to server
     * @param text Data for sending
     * @return Write future. Will fire when the data is sent.
     */
    public ChannelFuture send(String text);

}
