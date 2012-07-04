package io.rocketeer.server;

import io.rocketeer.protocol.ProtocolDef;

import java.util.Set;

/**
 * @author Heiko Braun
 * @date 6/29/12
 */
public interface ProtocolRegistry {
    Set<ProtocolDef> getSupportedSubprotocols();

}
