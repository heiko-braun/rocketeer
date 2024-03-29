package io.rocketeer;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public interface ContainerCallback {
    public void onConnect(ChannelHandlerContext context);

    public void onDisconnect(ChannelHandlerContext context);

    public void onMessage(ChannelHandlerContext context, WebSocketFrame text);

    public void onError(ChannelHandlerContext context, Throwable t);
}
