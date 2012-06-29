package io.rocketeer.server;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import io.rocketeer.ContainerCallback;
import io.rocketeer.protocol.ProtocolDef;
import org.jboss.netty.channel.ChannelHandler;
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
    private ProtocolRegistry registry;

    public WebSocketServerPipelineFactory(
            ExecutionHandler executionHandler,
            ContainerCallback callback, ProtocolRegistry registry) {
        this.callback = callback;
        this.execHandler = executionHandler;
        this.registry = registry;

    }

    public ChannelPipeline getPipeline() throws Exception {

        // Create a default pipeline implementation.
        final ChannelPipeline pipeline = pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("handler", new WebSocketServerHandler(callback, registry));

        // insert protocols
        for(ProtocolDef def : registry.getSupportedSubprotocols())
        {
            int i=0;
            for(ChannelHandler handler : def.getHandlerList())
            {
                pipeline.addLast(def.getName()+"_"+i, handler);
                i++;
            }
        }

        // invocation happens behind execution handler
        pipeline.addLast("exec", execHandler);
        pipeline.addLast("invocation", new InvocationHandler(callback));

        return pipeline;
    }
}

