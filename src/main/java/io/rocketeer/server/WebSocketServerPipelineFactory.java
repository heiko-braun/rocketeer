package io.rocketeer.server;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import io.rocketeer.ContainerCallback;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import static org.jboss.netty.channel.Channels.pipeline;

public class WebSocketServerPipelineFactory implements ChannelPipelineFactory {

    private ContainerCallback callback;

    public WebSocketServerPipelineFactory(ContainerCallback callback) {
        this.callback = callback;
    }

    public ChannelPipeline getPipeline() throws Exception {

        // Create a default pipeline implementation.
        final ChannelPipeline pipeline = pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("handler", new WebSocketServerHandler(callback));

        // SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        // engine.setUseClientMode(false);
        // pipeline.addLast("ssl", new SslHandler(engine));

        return pipeline;
    }
}

