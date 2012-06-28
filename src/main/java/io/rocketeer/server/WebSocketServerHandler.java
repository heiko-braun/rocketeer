package io.rocketeer.server;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import io.rocketeer.Endpoint;
import io.rocketeer.MessageListener;
import io.rocketeer.NettySession;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelLocal;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
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

public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {

    private final static Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private WebSocketServerHandshaker handshaker;
    private InvocationManager<NettySession> invocationContext;

    private static final ChannelLocal<String> channelSessionId = new ChannelLocal<String>();

    public WebSocketServerHandler(InvocationManager invocationContext) {
        this.invocationContext = invocationContext;
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

            // find matching web context
            boolean didMatch = false;

            for(String webContext : invocationContext.getEndpoints().keySet())
            {
                if(webContext.equals(req.getUri()))
                {
                    logger.info("Matching web context/id: '{}' => {}", webContext, ctx.getChannel().getId());
                    didMatch = true;
                    beginHandshake(ctx, req, webContext);
                    break;
                }
            }

            if(!didMatch)
            {
                HttpResponse res = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
                sendHttpResponse(ctx, req, res);
                return;
            }

        }

    }

    private void beginHandshake(final ChannelHandlerContext ctx, final HttpRequest req, final String webContext) {

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req, webContext), null, false);

        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
        } else {
            final ChannelFuture handshake = handshaker.handshake(ctx.getChannel(), req);

            /**
             * Create session and notify endpoint
             */
            handshake.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        Channels.fireExceptionCaught(future.getChannel(), future.getCause());
                    }
                    else {
                        future.awaitUninterruptibly();
                        final Endpoint endpoint = invocationContext.getEndpoints().get(webContext);

                        final NettySession session = new NettySession(ctx, endpoint);
                        channelSessionId.set(ctx.getChannel(), session.getId());

                        session.setProtocolVersion(req.getHeader(HttpHeaders.Names.SEC_WEBSOCKET_VERSION));

                        invocationContext.getSessions().add(session);

                        // identify the delegate
                        endpoint.hasOpened(session);
                    }
                }
            });
        }
    }

    private void handleWebSocketFrame(final ChannelHandlerContext ctx, final WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            logger.debug("Closing connection {}", ctx.getChannel().getId());
            handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
            ctx.getChannel().close();
            return;
        } else if (frame instanceof PingWebSocketFrame) {
            ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
            return;
        } else if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(
                    String.format("%s frame types not supported", frame.getClass().getName())
            );
        }

        /**
         * delegate to actual endpoint implementation
         */
        final String sessionId = channelSessionId.get(ctx.getChannel());

        for(NettySession session : invocationContext.getSessions())
        {
            if(sessionId.equals(session.getId()))
            {
                for(MessageListener listener : session.getListeners())
                {
                    if(listener instanceof MessageListener.Text)
                    {
                        ((MessageListener.Text)listener).onMessage(
                                ((TextWebSocketFrame)frame).getText()
                        );
                    }
                }
                break;
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
        logger.error("Exception caught, closing channel", e.getCause());
        e.getChannel().close();
    }

    private String getWebSocketLocation(HttpRequest req, String webContext) {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + webContext;
    }
}

