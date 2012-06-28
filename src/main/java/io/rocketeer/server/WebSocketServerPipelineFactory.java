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
import org.jboss.netty.handler.execution.ExecutionHandler;

import static org.jboss.netty.channel.Channels.pipeline;

public class WebSocketServerPipelineFactory implements ChannelPipelineFactory {

    private ContainerCallback callback;
    private ExecutionHandler execHandler;

    public WebSocketServerPipelineFactory(ExecutionHandler executionHandler, ContainerCallback callback) {
        this.callback = callback;
        this.execHandler = executionHandler;
    }

    public ChannelPipeline getPipeline() throws Exception {

        // Create a default pipeline implementation.
        final ChannelPipeline pipeline = pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());

        // todo: move after ws handler when invocation is abstracted into own handler
        pipeline.addLast("exec", execHandler);
        pipeline.addLast("handler", new WebSocketServerHandler(callback));

        // SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        // engine.setUseClientMode(false);
        // pipeline.addLast("ssl", new SslHandler(engine));

        return pipeline;
    }
}

