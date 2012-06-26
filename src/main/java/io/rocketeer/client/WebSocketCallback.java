package io.rocketeer.client;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface WebSocketCallback {
    public void onConnect(ChannelHandlerContext context);

    public void onDisconnect(ChannelHandlerContext context);

    public void onMessage(WebSocketClient client, WebSocketFrame text);

    public void onError(Throwable t);
}
