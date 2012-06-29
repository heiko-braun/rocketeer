
package io.rocketeer.protocol.stomp;

import io.rocketeer.protocol.stomp.StompFrame.Command;

import java.util.Set;

import static io.rocketeer.protocol.stomp.StompFrame.Header.CONTENT_LENGTH;

public class FrameHeader {

    public FrameHeader() {

    }

    public FrameHeader(Command command) {
        this.command = command;
    }
    
    public FrameHeader(Command command, Headers headers) {
        this.command = command;
        this.headers.putAll( headers );
    }
    
    public boolean isContentFrame() {
        return this.command.hasContent();
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return this.command;
    }

    public void set(String name, String value) {
        this.headers.put( name, value );
    }

    public String get(String name) {
        return this.headers.get( name );
    }
    
    public Set<String> getNames() {
        return this.headers.keySet();
    }
    
    public Headers getMap() {
        return this.headers;
    }

    public int getContentLength() {
        String value = get( CONTENT_LENGTH.toString() );
        if (value == null) {
            return -1;
        }

        return Integer.parseInt( value );
    }
    
    public String toString() {
        return "[FrameHeader: command=" + this.command + "; headers=" + this.headers + "]";
    }

    private Command command;
    private DefaultHeaders headers = new DefaultHeaders();
}
