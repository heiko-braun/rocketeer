package io.rocketeer.client;

import io.rocketeer.ClientConfiguration;
import io.rocketeer.ClientContainer;
import io.rocketeer.Endpoint;
import io.rocketeer.MessageListener;
import io.rocketeer.Session;
import io.rocketeer.NettySession;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Heiko Braun
 * @date 6/27/12
 */
public class ClientContainerImpl implements ClientContainer {

    private final static Logger log = LoggerFactory.getLogger(ClientContainerImpl.class);

    private Map<String, Endpoint> endpoints = new HashMap<String, Endpoint>();
    private List<NettySession> sessions = new CopyOnWriteArrayList<NettySession>();

    public void connect(final Endpoint endpoint, final ClientConfiguration config) {
        WebSocketClientFactory clientFactory = new WebSocketClientFactory();

        WebSocketClient client = clientFactory.newClient(
                config.getURI(),
                new WebSocketCallback() {
                    public void onConnect(ChannelHandlerContext context) {
                        provideSession(context, endpoint, config);
                    }

                    public void onDisconnect(ChannelHandlerContext context) {
                        NettySession session = findSession(context.getChannel().getId());
                        if(session!=null)
                        {
                            session.getEndpoint().hasClosed(session);
                        }
                    }

                    public void onMessage(ChannelHandlerContext context, WebSocketFrame text) {
                        NettySession session = findSession(context.getChannel().getId());
                        if(session!=null)
                        {
                            for(MessageListener listener : session.getListeners())
                            {
                                ((MessageListener.Text)listener).onMessage(
                                        ((TextWebSocketFrame)text).getText()
                                );
                            }
                        }
                    }

                    public void onError(ChannelHandlerContext context, Throwable t) {
                        NettySession session = findSession(context.getChannel().getId());
                        if(session!=null)
                        {
                            session.getEndpoint().handleError(t, session);
                        }
                    }
                }
        );

        client.connect().awaitUninterruptibly();
    }

    private NettySession findSession(Integer id)
    {
        NettySession match = null;
        for(NettySession session : sessions)
        {
            if(session.getId().equals(id))
            {
                match = session;
                break;
            }
        }
        return match;
    }

    private void provideSession(ChannelHandlerContext context, Endpoint endpoint, ClientConfiguration config) {
        NettySession session = new NettySession(context, endpoint);
        log.debug("Session created {}", session.getId());

        sessions.add(session);
        endpoints.put(config.getURI().toString(), endpoint);

        endpoint.hasOpened(session);
    }

    public List<? extends Session> getActiveSessions() {
        return Collections.unmodifiableList(sessions);
    }
}
