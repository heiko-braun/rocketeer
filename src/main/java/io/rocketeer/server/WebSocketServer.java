package io.rocketeer.server;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import io.rocketeer.ClientConfiguration;
import io.rocketeer.Endpoint;
import io.rocketeer.ServerConfiguration;
import io.rocketeer.ServerContainer;
import io.rocketeer.Session;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
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
public class WebSocketServer implements ServerContainer, InvocationContext<NettySession> {

    private final static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    private ExecutorService bossExecutor;
    private ExecutorService workerExecutor;

    private Integer portNumber = 8080;

    private ServerBootstrap bootstrap;

    private Map<String, Endpoint> endpoints = new HashMap<String, Endpoint>();
    private List<NettySession> sessions = new CopyOnWriteArrayList<NettySession>();

    public WebSocketServer(Integer portNumber) {
        this.portNumber = portNumber;

        // netty logging
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    }

    public void registerServer(Endpoint endpoint, ServerConfiguration config) {
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
            bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory(this));

            bootstrap.setOption("tcpNoDelay", true);
            bootstrap.setOption("keepAlive", true);

            // Bind and start to accept incoming connections.
            bootstrap.bind(new InetSocketAddress(portNumber));

            logger.info("Started server container on port: " + portNumber);


        } catch (final Exception e) {
            logger.error("Failed to start server", e);
        }
    }

    public void stop() {
        bootstrap.releaseExternalResources();
        logger.info("Server successfully shutdown.");
    }
}

