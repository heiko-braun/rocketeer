package io.rocketeer.server;

import org.jboss.netty.channel.ChannelLocal;

/**
 * @author Heiko Braun
 * @date 6/28/12
 */
public class ChannelRef {

    public static final ChannelLocal<String> sessionId = new ChannelLocal<String>();
    public static final ChannelLocal<String> webContext = new ChannelLocal<String>();
}
