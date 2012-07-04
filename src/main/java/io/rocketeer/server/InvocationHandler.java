package io.rocketeer.server;

import io.rocketeer.ContainerCallback;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Heiko Braun
 * @date 6/28/12
 */
public class InvocationHandler extends SimpleChannelUpstreamHandler {

    private final static Logger log = LoggerFactory.getLogger(InvocationHandler.class);
    private ContainerCallback callback;
    private ChannelBuffer fragmentBuffer;
    private boolean initialFrameBinary;

    public InvocationHandler(ContainerCallback callback) {
        this.callback = callback;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        Object msg = e.getMessage();
        if (msg instanceof WebSocketFrame) {

            WebSocketFrame frame = (WebSocketFrame)msg;

            // the opening frame of a sequence
            if(((frame instanceof BinaryWebSocketFrame)
                    || (frame instanceof TextWebSocketFrame))
                    && !frame.isFinalFragment())
            {
                initialFrameBinary = (frame instanceof BinaryWebSocketFrame);
                fragmentBuffer = ChannelBuffers.dynamicBuffer(128);
                fragmentBuffer.writeBytes(frame.getBinaryData());
            }

            // one of several data frames within a sequence
            else if(!frame.isFinalFragment() && fragmentBuffer!=null)
            {
                fragmentBuffer.writeBytes(frame.getBinaryData());
            }

            // // the last frame of a sequence
            else if(frame.isFinalFragment() && fragmentBuffer!=null)
            {
                WebSocketFrame aggregate = initialFrameBinary ?
                        new BinaryWebSocketFrame(fragmentBuffer) : new TextWebSocketFrame(fragmentBuffer);

                callback.onMessage(ctx, aggregate);
                fragmentBuffer = null;
            }

            // regular simple frames
            else
            {
                callback.onMessage(ctx, (WebSocketFrame)msg);
            }


        }
    }
}
