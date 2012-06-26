package io.rocketeer.server;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class WebSocketServer {

    private Integer portNumber = 8080;

    public void start() {
        try {

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

