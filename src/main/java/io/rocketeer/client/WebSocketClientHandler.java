package io.rocketeer.client;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import io.rocketeer.ContainerCallback;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.util.CharsetUtil;

/**
 * Handles socket communication for a connected WebSocket client
 * Not intended for end-users. Please use {@link WebSocketClient}
 * or {@link io.rocketeer.ContainerCallback} for controlling your client.
 *
 */
public class WebSocketClientHandler extends SimpleChannelUpstreamHandler {

    private ContainerCallback callback;
    private final WebSocketClientHandshaker handshaker;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, ContainerCallback callback) {
        this.handshaker = handshaker;
        this.callback = callback;
    }

    @Override
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
            System.out.println("Client received closing frame");
            ch.close();
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
