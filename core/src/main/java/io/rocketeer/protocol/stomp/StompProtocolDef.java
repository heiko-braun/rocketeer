package io.rocketeer.protocol.stomp;

import io.rocketeer.protocol.ProtocolDef;

/**
 * @author Heiko Braun
 * @date 6/29/12
 */
public class StompProtocolDef extends ProtocolDef {

    public StompProtocolDef() {
        super("stomp");

        addHandler(new StompFrameDecoder());
        addHandler(new StompFrameEncoder());
        addHandler(new StompFrameHandler());
    }
}
