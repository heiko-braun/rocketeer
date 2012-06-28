package io.rocketeer.server;

import io.rocketeer.ContainerCallback;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Heiko Braun
 * @date 6/28/12
 */
public class InvocationHandler extends SimpleChannelUpstreamHandler {
    private final static Logger logger = LoggerFactory.getLogger(InvocationHandler.class);
    private ContainerCallback callback;

    public InvocationHandler(ContainerCallback callback) {
        this.callback = callback;
    }

    @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

            Object msg = e.getMessage();
            if (msg instanceof WebSocketFrame) {
                callback.onMessage(ctx, (WebSocketFrame)msg);
            }
        }
}
