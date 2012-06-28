package io.rocketeer.server;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import io.rocketeer.ClientConfiguration;
import io.rocketeer.ContainerCallback;
import io.rocketeer.Endpoint;
import io.rocketeer.MessageListener;
import io.rocketeer.NettySession;
import io.rocketeer.ServerConfiguration;
import io.rocketeer.ServerContainer;
import io.rocketeer.Session;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This server illustrates support for the different web socket specification versions
 * and will work with:
 *
 * <ul>
 *  <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
 *  <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
 *  <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
 *  <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 *  <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
 *  <li>Firefox 11+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * </ul>
 */
public class WebSocketServer implements ServerContainer, InvocationManager<NettySession> {

    private final static Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private ExecutorService bossExecutor;
    private ExecutorService workerExecutor;

    private Integer portNumber = 8080;

    private ServerBootstrap bootstrap;

    private Map<String, Endpoint> endpoints = new HashMap<String, Endpoint>();
    private List<NettySession> sessions = new CopyOnWriteArrayList<NettySession>();

    private Channel mainChannel;

    public WebSocketServer(Integer portNumber) {
        this.portNumber = portNumber;

        // netty logging
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    }

    public void registerEndpoint(Endpoint endpoint, ServerConfiguration config) {
        endpoints.put(config.getURI().toString(), endpoint);
    }

    public void connect(Endpoint endpoint, ClientConfiguration olc) {
        throw new RuntimeException("Not implemented yet");
    }

    public List<? extends Session> getActiveSessions() {
        return Collections.unmodifiableList(sessions);
    }

    public Map<String, Endpoint> getEndpoints() {
        return Collections.unmodifiableMap(endpoints);
    }

    public List<NettySession> getSessions() {
        return sessions;
    }

    public void start() {
        try {
            bossExecutor = Executors.newFixedThreadPool(5);
            workerExecutor = Executors.newFixedThreadPool(25);

            bootstrap = new ServerBootstrap(
                    new NioServerSocketChannelFactory(
                            bossExecutor,
                            workerExecutor
                    )
            );

            // Set up the event pipeline factory.
            bootstrap.setPipelineFactory(
                    new WebSocketServerPipelineFactory(new ContainerCallback() {
                        public void onConnect(ChannelHandlerContext context) {

                            final Endpoint endpoint = endpoints.get(ChannelRef.webContext.get(context.getChannel()));

                            final NettySession session = new NettySession(context, endpoint);
                            ChannelRef.sessionId.set(context.getChannel(), session.getId());

                            log.debug("Created session for web context '{}': {}",
                                    ChannelRef.webContext.get(context.getChannel()), session.getId());

                            sessions.add(session);

                            // notify delegate
                            endpoint.hasOpened(session);
                        }

                        public void onDisconnect(ChannelHandlerContext context) {
                            final NettySession session = findSession(context.getChannel());
                            if(session.isActive())
                                session.close();
                            sessions.remove(session);
                        }

                        public void onMessage(ChannelHandlerContext context, WebSocketFrame frame) {
                            final NettySession session = findSession(context.getChannel());
                            for(MessageListener listener : session.getListeners())
                            {
                                if(listener instanceof MessageListener.Text)
                                {
                                    ((MessageListener.Text)listener).onMessage(
                                            ((TextWebSocketFrame)frame).getText()
                                    );
                                }
                            }
                        }

                        public void onError(ChannelHandlerContext context, Throwable t) {

                        }
                    })
            );


            bootstrap.setOption("tcpNoDelay", true);
            bootstrap.setOption("keepAlive", true);

            // Bind and start to accept incoming connections.
            mainChannel = bootstrap.bind(new InetSocketAddress(portNumber));

            log.info("Started server container on port: " + portNumber);


        } catch (final Exception e) {
            log.error("Failed to start server", e);
        }
    }

    private NettySession findSession(Channel channel)
    {
        String sessionId = ChannelRef.sessionId.get(channel);
        NettySession match = null;
        for(NettySession session : sessions)
        {
            if(session.getId().equals(sessionId))
            {
                match = session;
                break;
            }
        }

        if(null==match)
            log.warn("no session with id {}", sessionId);

        return match;
    }

    public void stop() {
        mainChannel.close();
        bootstrap.releaseExternalResources();
        log.info("Server successfully shutdown.");
    }
}

