package io.rocketeer.server;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import io.rocketeer.ContainerCallback;
import io.rocketeer.protocol.ProtocolDef;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class WebSocketServerHandler extends SimpleChannelHandler {

    private final static Logger log = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private WebSocketServerHandshaker handshaker;
    private ContainerCallback callback;
    private ProtocolRegistry protocolRegistry;

    private boolean isClosing = false;

    public WebSocketServerHandler(ContainerCallback callback, ProtocolRegistry protocolRegistry) {
        this.callback = callback;
        this.protocolRegistry = protocolRegistry;
    }

    @Override
    public void disconnectRequested(final ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {

        log.debug("Disconnect requested ...");
        if(!isClosing && ctx.getChannel().isOpen())
        {
            sendClosingFrame(ctx, new CloseWebSocketFrame());
            isClosing = true;
        }
    }

    private void sendClosingFrame(final ChannelHandlerContext ctx, CloseWebSocketFrame frame) {

        final ChannelFuture channelFuture = handshaker.close(ctx.getChannel(), frame);

        channelFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                String sessionId = ChannelRef.sessionId.get(ctx.getChannel());
                if(future.isSuccess())
                    log.debug("Server did send closing frame {}", sessionId);
            }
        });
    }

    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        callback.onDisconnect(ctx);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, HttpRequest req) throws Exception {

        // Allow only GET methods.
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Send the demo page and favicon.ico
        if (req.getUri().equals("/")) {
            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);

            ChannelBuffer content = WebSocketIndexPage.getContent();

            res.setHeader(CONTENT_TYPE, "text/html; charset=UTF-8");
            setContentLength(res, content.readableBytes());

            res.setContent(content);
            sendHttpResponse(ctx, req, res);
            return;
        } else if (req.getUri().equals("/favicon.ico")) {
            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }
        else
        {
            // associate the initial web context used when engaging the handshake
            ChannelRef.webContext.set(ctx.getChannel(), req.getUri());

            beginHandshake(ctx, req);
        }

    }

    private String getProtocolString() {
        StringBuffer sb = new StringBuffer();
        int i=0;
        for(ProtocolDef def : protocolRegistry.getSupportedSubprotocols())
        {
            sb.append(def.getName());
            if(i<protocolRegistry.getSupportedSubprotocols().size()-1)
                sb.append(", ");
            i++;
        }
        return sb.toString();
    }

    private void beginHandshake(final ChannelHandlerContext ctx, final HttpRequest req) {

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), getProtocolString(), false);

        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
        } else {

            // if the handshake fails it will throw en exception ...
            final ChannelFuture handshake = handshaker.handshake(ctx.getChannel(), req);

            // if handshake succeeds create the session
            handshake.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {

                    // at this point the subprotocol should be selected
                    ChannelRef.subprotocol.set(ctx.getChannel(), handshaker.getSelectedSubprotocol());

                    if (!future.isSuccess()) {
                        Channels.fireExceptionCaught(future.getChannel(), future.getCause());
                    }
                    else {
                        future.awaitUninterruptibly();
                        callback.onConnect(ctx);
                    }
                }
            });

        }
    }

    private void handleWebSocketFrame(final ChannelHandlerContext ctx, final WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {

            if(!isClosing)
            {
                sendClosingFrame(ctx, (CloseWebSocketFrame)frame);
                ctx.getChannel().close();
            }

            return;
        } else if (frame instanceof PingWebSocketFrame) {
            ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
            return;
        }
        else {

            if((frame instanceof TextWebSocketFrame)
                    || (frame instanceof BinaryWebSocketFrame)
                    || (frame instanceof ContinuationWebSocketFrame))
            {
                // forward to invocation handler
                ctx.sendUpstream(
                        new UpstreamMessageEvent(
                                ctx.getChannel(),
                                frame,
                                ctx.getChannel().getRemoteAddress()
                        )
                );
            }
        }

    }


    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        // Generate an error page if response status code is not OK (200).
        if (res.getStatus().getCode() != 200) {
            res.setContent(
                    ChannelBuffers.copiedBuffer(
                            res.getStatus().toString(), CharsetUtil.UTF_8));
            setContentLength(res, res.getContent().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.getChannel().write(res);
        if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        log.error("Exception caught, closing channel", e.getCause());
        e.getChannel().close();
    }

    private String getWebSocketLocation(HttpRequest req) {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + req.getUri();
    }
}

