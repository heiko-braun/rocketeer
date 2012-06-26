package io.rocketeer.server;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.JdkLoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class WebSocketServer {

    private Integer portNumber = 8080;

    public void start() {
        try {

            InternalLoggerFactory.setDefaultFactory(new JdkLoggerFactory());
            Logger.getLogger("io.netty").setLevel(Level.FINE);

            final ServerBootstrap
                    bootstrap = new ServerBootstrap(
                    new NioServerSocketChannelFactory(
                            Executors.newCachedThreadPool(),
                            Executors.newCachedThreadPool()
                    )
            );

            // Set up the event pipeline factory.
            bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory());

            // Bind and start to accept incoming connections.
            bootstrap.bind(new InetSocketAddress(portNumber));

            System.out.println("-=[ STARTED ]=- on port#: " + portNumber);

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
}

