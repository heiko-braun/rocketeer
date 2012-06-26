package io.rocketeer.server;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

// PoC WS server frame event emulator, naive impl!
public final class EnvStatsProcessor {

    final static Logger logger = LoggerFactory.getLogger(EnvStatsProcessor.class);

    private static final char DLM = '|';

    public static void handleRequest(final ChannelHandlerContext ctx, final TextWebSocketFrame frame) {
        try {

            String req = frame.getText();

            logger.debug("serving: " + req);

            if (req.equals("gc")) {
                System.gc();
            } else if (req.equals("stop")) {
                //todo should stop running threads, etc
            } else if (req.equals("init")) {
                spawnSysProcess(ctx.getChannel());
                if (System.getProperty("os.name", "").equalsIgnoreCase("linux")) {
                    spawnCPUProcess(ctx.getChannel());
                    spawnVMEMProcess(ctx.getChannel());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void spawnSysProcess(final Channel channel) {
        new Thread() {
            StringBuffer buf = null;

            @Override
            public void run() {
                if (channel == null) return;
                buf = new StringBuffer();
                try {
                    while (channel.isConnected()) {

                        Thread.sleep(500);

                        // sys|thread|os|procs|who|ver|nano|free|used|total|max
                        buf.append("sys").append(DLM).
                                append(Thread.currentThread().getName()).append(DLM).
                                append(System.getProperty("os.name")).append(DLM).
                                append(Runtime.getRuntime().availableProcessors()).append(DLM).
                                append(System.getenv("LOGNAME")).append(DLM).
                                append(System.getProperty("java.vm.name")).append(DLM).
                                append(System.nanoTime()).append(DLM).
                                append(Runtime.getRuntime().freeMemory()).append(DLM).
                                append(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).append(DLM).
                                append(Runtime.getRuntime().totalMemory()).append(DLM).
                                append(Runtime.getRuntime().maxMemory());

                        if (channel.isOpen())
                            channel.write(new TextWebSocketFrame(buf.toString()));

                        buf.setLength(0);

                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static void spawnCPUProcess(final Channel channel) {
        new Thread() {
            StringBuffer buf = null;

            @Override
            public void run() {
                if (channel == null) return;
                buf = new StringBuffer();
                try {
                    while (channel.isConnected()) {

                        Thread.sleep(1000);

                        // cpu|thread|
                        buf.append("cpu").append(DLM).
                                append(Thread.currentThread().getName()).append(DLM).
                                append(execCmd(channel, "cat /proc/stat"));

                        if (channel.isOpen())
                            channel.write(new TextWebSocketFrame(buf.toString()));

                        buf.setLength(0);

                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static void spawnVMEMProcess(final Channel channel) {
        new Thread() {
            StringBuffer buf = null;

            @Override
            public void run() {
                if (channel == null) return;
                buf = new StringBuffer();
                try {
                    while (channel.isConnected()) {

                        Thread.sleep(1000);

                        // vmem|thread|
                        buf.append("vmem").append(DLM).
                                append(Thread.currentThread().getName()).append(DLM).
                                append(execCmd(channel, "vmstat"));

                        if (channel.isOpen())
                            channel.write(new TextWebSocketFrame(buf.toString()));

                        buf.setLength(0);

                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static String execCmd(final Channel channel, final String cmd) {
        StringBuffer buf = null;
        Process p = null;
        try {
            buf = new StringBuffer();
            p = Runtime.getRuntime().exec(cmd, getEnv(), new File("."));
            buf.append(new String(sink(p.getInputStream()))).append(DLM);
            buf.append(new String(sink(p.getErrorStream())));
            p.waitFor();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return buf.toString();
    }

    private static byte[] sink(final InputStream in) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (in == null) return baos.toByteArray();
        try {
            while (true) {
                final int bb = in.read();
                if (bb < 0) {
                    break;
                }
                baos.write(bb);
            }
        } catch (final Exception e) {
        }
        return baos.toByteArray();
    }

    private static String[] getEnv() {
        final Iterator<String> i = System.getenv().keySet().iterator();
        final String[] ret = new String[System.getenv().keySet().size()];
        int c = 0;
        while (i.hasNext()) {
            final String k = i.next();
            ret[c] = k + "=" + System.getenv(k);
            c++;
        }
        return ret;
    }

}


