package io.rocketeer;

import java.net.URI;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public class EndpointConfiguration {

    private URI uri;

    public EndpointConfiguration(URI uri) {
        this.uri = uri;
    }

    public java.net.URI getURI() {
        return uri;
    }
}
