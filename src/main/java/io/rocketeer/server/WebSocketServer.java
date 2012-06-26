package io.rocketeer.server;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import io.rocketeer.ClientConfiguration;
import io.rocketeer.Endpoint;
import io.rocketeer.ServerConfiguration;
import io.rocketeer.ServerContainer;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * This server illustrates support for the different web socket specification versions and will work with:
 *
 * <ul>
 * <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Firefox 11+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * </ul>
 */
public class WebSocketServer implements ServerContainer {

    Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    private Integer portNumber = 8080;
    private ServerBootstrap bootstrap;
    private Map<String, Endpoint> endpoints = new HashMap<String, Endpoint>();

    public WebSocketServer(Integer portNumber) {
        this.portNumber = portNumber;
    }

    public WebSocketServer() {
    }

    public void registerServer(Endpoint endpoint, ServerConfiguration ilc) {

        endpoints.put(ilc.getURI().toString(), endpoint);
    }

    public void connect(Endpoint endpoint, ClientConfiguration olc) {
        throw new RuntimeException("Not implemented yet");
    }

    public void start() {
        try {
            // netty logging
            InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

            bootstrap = new ServerBootstrap(
                    new NioServerSocketChannelFactory(
                            Executors.newCachedThreadPool(),
                            Executors.newCachedThreadPool()
                    )
            );

            // Set up the event pipeline factory.
            bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory(endpoints));

            // Bind and start to accept incoming connections.
            bootstrap.bind(new InetSocketAddress(portNumber));

            logger.info("Started server container on port#: " + portNumber);


        } catch (final Exception e) {
            e.printStackTrace();
        }
    }


    public Integer getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(final Integer portNumber) {
        this.portNumber = portNumber;
    }

    ////////////////////////////////////////////////////////////
    public static void main(final String[] args) {
        final WebSocketServer websok = new WebSocketServer();
        // websok.setPortNumber( Integer.getInteger( args[0]) );
        websok.start();
    }

    public void stop() {
        bootstrap.releaseExternalResources();
    }
}

