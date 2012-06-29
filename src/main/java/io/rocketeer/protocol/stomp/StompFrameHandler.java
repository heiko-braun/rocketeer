package io.rocketeer.protocol.stomp;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.rocketeer.protocol.stomp.StompFrame.Command.*;

/**
 * @author Heiko Braun
 * @date 6/29/12
 */
public class StompFrameHandler extends SimpleChannelHandler {

    private final static Logger log = LoggerFactory.getLogger(StompFrameHandler.class);

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if(e.getMessage() instanceof StompFrame)
        {
            StompFrame frame = (StompFrame)e.getMessage();
            log.debug("Received {}", frame);

            if(frame.getCommand().equals(CONNECT))
            {
                ctx.getChannel().write(new StompFrame(CONNECTED));
            }
            else if(frame.getCommand().equals(DISCONNECT))
            {
                String receiptId = frame.getHeader( StompFrame.Header.RECEIPT );
                if (receiptId != null) {
                    ChannelFuture future = ctx.getChannel().write(
                            StompFrames.newReceiptFrame(receiptId)
                    );
                    future.addListener( new ChannelFutureListener() {
                        public void operationComplete(ChannelFuture future) throws Exception {
                            Channels.disconnect(ctx.getChannel());
                        }
                    } );
                } else {
                    Channels.disconnect(ctx.getChannel());
                }
            }
        }
        else
        {
            ctx.sendUpstream(e);
        }
    }
}
