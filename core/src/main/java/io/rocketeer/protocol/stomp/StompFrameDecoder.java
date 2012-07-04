package io.rocketeer.protocol.stomp;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class StompFrameDecoder extends OneToOneDecoder {

    private static final Charset UTF_8 = Charset.forName( "UTF-8" );
    private static Logger log = LoggerFactory.getLogger(StompFrameDecoder.class);

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            WebSocketFrame webSocketFrame = (WebSocketFrame) msg;
            ChannelBuffer buffer = webSocketFrame.getBinaryData();
            StompFrame stompFrame = StompFrameCodec.INSTANCE.decode( buffer );
            return stompFrame;
        }
        return msg;
    }

}

