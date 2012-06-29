package io.rocketeer.client;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import io.rocketeer.ContainerCallback;
import io.rocketeer.server.ChannelRef;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles socket communication for a connected WebSocket client
 * Not intended for end-users. Please use {@link WebSocketClient}
 * or {@link io.rocketeer.ContainerCallback} for controlling your client.
 *
 */
public class WebSocketClientHandler extends SimpleChannelHandler {

    private final static Logger log = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private ContainerCallback callback;
    private final WebSocketClientHandshaker handshaker;

    private boolean isClosing = false;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, ContainerCallback callback) {
        this.handshaker = handshaker;
        this.callback = callback;
    }

    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {

         // TODO: closing can be initiated from both sides
        if(!isClosing && ctx.getChannel().isOpen())
        {
            final ChannelFuture channelFuture = ctx.getChannel().write(
                    new CloseWebSocketFrame(1000, "Bye")
            );

            channelFuture.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    String sessionId = ChannelRef.sessionId.get(ctx.getChannel());
                    if(future.isSuccess())
                        log.debug("Client did send closing frame {}", sessionId);
                }
            });

            isClosing = true;
        }

    }

    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        callback.onDisconnect(ctx);
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Channel ch = ctx.getChannel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (HttpResponse) e.getMessage());
            callback.onConnect(ctx);
            return;
        }

        if (e.getMessage() instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) e.getMessage();
            throw new Exception("Unexpected HttpResponse (status=" + response.getStatus() + ", content="
                    + response.getContent().toString(CharsetUtil.UTF_8) + ")");
        }


        WebSocketFrame frame = (WebSocketFrame) e.getMessage();

        // TODO
        if (frame instanceof PongWebSocketFrame)
        {
            System.out.println("Client received pong frame");
        }
        else if (frame instanceof CloseWebSocketFrame) {

            // TODO: closing may be initiated from both sides
            // check the state and eventually respond with a closing frame
            log.debug("Client did receive closing frame {}", ChannelRef.sessionId.get(ctx.getChannel()));
        }
        else
        {
            // TODO
            callback.onMessage(ctx, frame);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        final Throwable t = e.getCause();
        callback.onError(ctx, t);
        e.getChannel().close();
    }
}
