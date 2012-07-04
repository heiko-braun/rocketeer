package io.rocketeer.protocol.stomp;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.ReadOnlyChannelBuffer;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
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
                // If the accept-version header is missing,
                // it means that the client only supports version 1.0 of the protocol.
                String acceptVersion = frame.getHeader(StompFrame.Header.VERSION) != null ?
                        frame.getHeader(StompFrame.Header.VERSION) : "1.0";

                if(acceptVersion.indexOf("1.1") == -1)
                {
                    final String errMsg = "Unsupported stomp protocol version: " + acceptVersion;
                    log.warn(errMsg);
                    final StompContentFrame error = new StompContentFrame(ERROR);
                    error.setContent(ChannelBuffers.copiedBuffer(errMsg.getBytes()));
                    ctx.getChannel().write(error);
                    Channels.disconnect(ctx.getChannel());
                    return;
                }

                // TODO: Stomp 1.1 supports authentication ('login', 'passcode' headers)
                final StompFrame connected = new StompFrame(CONNECTED);
                connected.getHeaders().put(StompFrame.Header.VERSION, "1.1");
                ctx.getChannel().write(connected);
            }
            else if(frame.getCommand().equals(DISCONNECT))
            {

                // send receipt and notify web socket handler to close the physical connection

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
