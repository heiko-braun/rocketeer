package io.rocketeer.protocol.stomp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultHeaders extends HashMap<String, String> implements Headers {

    private static final long serialVersionUID = 1L;

    public String get(String headerName) {
        return super.get( headerName );
    }

    public void putAll(Headers headers) {
        for (String name : headers.getHeaderNames()) {
            put( name, headers.get( name ) );
        }
    }

    public void remove(String headerName) {
        super.remove( headerName );
    }

    public Set<String> getHeaderNames() {
        return keySet();
    }

    public Headers duplicate() {
        DefaultHeaders dupe = new DefaultHeaders();
        dupe.putAll(  (Map<String,String>) this  );
        return dupe;
    }

}
