package io.rocketeer.protocol.stomp;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class StompFrameEncoder extends OneToOneEncoder {

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if ( msg instanceof StompFrame) {
            ChannelBuffer buffer = StompFrameCodec.INSTANCE.encode( (StompFrame) msg );
            WebSocketFrame webSocketFrame = new TextWebSocketFrame(buffer);
            return webSocketFrame;
        }
        return msg;
    }

}

