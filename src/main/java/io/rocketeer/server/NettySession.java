package io.rocketeer.server;

import io.rocketeer.Endpoint;
import io.rocketeer.MessageListener;
import io.rocketeer.RemoteEndpoint;
import io.rocketeer.Session;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public class NettySession implements Session {

    private final static Logger log = LoggerFactory.getLogger(NettySession.class);

    private ChannelHandlerContext ctx;

    private List<MessageListener> listeners = new ArrayList<MessageListener>();
    private Endpoint endpoint;

    private String protocolVersion;
    private String subprotocol;

    public NettySession(ChannelHandlerContext ctx, Endpoint endpoint) {
        this.ctx = ctx;
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
        //ctx.getChannel().write(new CloseWebSocketFrame());
        final ChannelFuture channelFuture = ctx.getChannel().close();
        channelFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess())
                    log.debug("Session close {}", getId());
                else
                    log.error("Failed to close Session {}", getId(), future.getCause());
            }
        });
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public List<MessageListener> getListeners() {
        return Collections.unmodifiableList(listeners);
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

    public boolean isActive() {
        return ctx!=null ? ctx.getChannel().isConnected() : false;
    }

    public Integer getId() {
        return ctx.getChannel().getId();
    }


}
