package io.rocketeer.protocol;

import org.jboss.netty.channel.ChannelHandler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Heiko Braun
 * @date 6/29/12
 */
public abstract class ProtocolDef {

    private String name;
    private List<ChannelHandler> handlerList = new LinkedList<ChannelHandler>();

    protected ProtocolDef(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ProtocolDef addHandler(ChannelHandler handler)
    {
        handlerList.add(handler);
        return this;
    }

    public List<ChannelHandler> getHandlerList() {
        return Collections.unmodifiableList(handlerList);
    }
}
