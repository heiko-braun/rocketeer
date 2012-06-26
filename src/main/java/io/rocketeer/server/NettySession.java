package io.rocketeer.server;

import io.rocketeer.Endpoint;
import io.rocketeer.MessageListener;
import io.rocketeer.RemoteEndpoint;
import io.rocketeer.Session;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public class NettySession implements Session {
    private ChannelFuture handshake;
    private ChannelHandlerContext ctx;

    private List<MessageListener> listeners = new ArrayList<MessageListener>();
    private String webContext;
    private Endpoint endpoint;

    public NettySession(ChannelHandlerContext ctx, ChannelFuture handshake, Endpoint endpoint) {

        this.ctx = ctx;
        this.handshake = handshake;
        this.endpoint = endpoint;

    }

    public void addMessageListener(final MessageListener listener) {
        listeners.add(listener);
    }

    public RemoteEndpoint getRemote() {
        return new RemoteEndpoint() {
            public void sendString(String text) {
                ctx.getChannel().write(new TextWebSocketFrame(text));
            }

            public void sendBytes(byte[] data) {
               throw new RuntimeException("Not implemented yet");
            }
        };
    }

    public void close() throws IOException {

    }

    Endpoint getEndpoint() {
        return endpoint;
    }

    List<MessageListener> getListeners() {
        return listeners;
    }
}
