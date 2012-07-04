
package io.rocketeer.protocol.stomp;

public class StompControlFrame extends StompFrame {

    public StompControlFrame(Command command) {
        super( command );
    }
    
    public StompControlFrame(Command command, Headers headers) {
        super( command, headers );
    }
    
    public StompControlFrame(FrameHeader header) {
        super( header );
    }

}
