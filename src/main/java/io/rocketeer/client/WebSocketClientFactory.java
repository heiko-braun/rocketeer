package io.rocketeer.client;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.Executors;

/**
 * A factory for creating WebSocket clients.
 * The entry point for creating and connecting a client.
 * Can and should be used to create multiple instances.
 *
 */
public class WebSocketClientFactory {

    private NioClientSocketChannelFactory socketChannelFactory = new NioClientSocketChannelFactory(
            Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());

    /**
     * Create a new WebSocket client
     * @param uri URL to connect to.
     * @param callback Callback interface to receive events
     * @return A WebSocket client. Call {@link WebSocketClient#connect()} to connect.
     */
    public WebSocketClient newClient(final URI uri, final WebSocketCallback callback) {

        final ClientBootstrap bootstrap = new ClientBootstrap(socketChannelFactory);

        String protocol = uri.getScheme();
        if (!protocol.equals("ws") && !protocol.equals("wss")) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        final WebSocketClientHandshaker handshaker =
                new WebSocketClientHandshakerFactory().newHandshaker(
                        uri, WebSocketVersion.V13, null, false, Collections.EMPTY_MAP);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();

                pipeline.addLast("decoder", new HttpResponseDecoder());
                pipeline.addLast("encoder", new HttpRequestEncoder());
                pipeline.addLast("ws-handler", new WebSocketClientHandler(handshaker, callback));
                return pipeline;
            }
        });

        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);

        return new WebSocketClientImpl(bootstrap, uri, handshaker);
    }

    class WebSocketClientImpl implements WebSocketClient {
        private Channel ch = null;
        private final WebSocketClientHandshaker handshaker;
        private final ClientBootstrap bootstrap;
        private final URI uri;

        WebSocketClientImpl(ClientBootstrap bootstrap, URI uri,WebSocketClientHandshaker handshaker) {
            this.bootstrap = bootstrap;
            this.uri = uri;
            this.handshaker = handshaker;
        }

        public ChannelFuture connect() {

            ChannelFuture future =
                    bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));
            future.syncUninterruptibly();

            ch = future.getChannel();
            try {
                handshaker.handshake(ch).syncUninterruptibly();
            } catch (Exception e) {
                throw new RuntimeException("Failed to connect", e);
            }

            return future;
        }

        public ChannelFuture disconnect() {
            return ch.write(new CloseWebSocketFrame(1000, "Bye"));
        }

        public ChannelFuture send(String text) {
            return ch.write(new TextWebSocketFrame(text));
        }
    }
}
