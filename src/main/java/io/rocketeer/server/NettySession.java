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

    private String protocolVersion;
    private String subprotocol;

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

    public void close(){

    }

    Endpoint getEndpoint() {
        return endpoint;
    }

    List<MessageListener> getListeners() {
        return listeners;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getSubprotocol() {
        return subprotocol;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public void setSubprotocol(String subprotocol) {
        this.subprotocol = subprotocol;
    }

    public long getTimeout() {
        throw new RuntimeException("Not implemented yet");
    }

    public void setTimeout(long seconds) {
        throw new RuntimeException("Not implemented yet");
    }
}