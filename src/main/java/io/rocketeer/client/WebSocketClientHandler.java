package io.rocketeer.client;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

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
 * or {@link WebSocketCallback} for controlling your client.
 *
 */
public class WebSocketClientHandler extends SimpleChannelUpstreamHandler {

    private WebSocketCallback callback;
    private final WebSocketClientHandshaker handshaker;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, WebSocketCallback callback) {
        this.handshaker = handshaker;
        this.callback = callback;
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("WebSocket Client disconnected!");
        callback.onDisconnect(ctx);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Channel ch = ctx.getChannel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (HttpResponse) e.getMessage());
            callback.onError(new RuntimeException("Handshake failed."));
            return;
        }

        // indicate success
        callback.onConnect(ctx);

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
            callback.onMessage(null, frame);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        final Throwable t = e.getCause();
        callback.onError(t);
        e.getChannel().close();
    }
}
