package io.rocketeer;

import java.net.URI;

/**
 * @author Heiko Braun
 * @date 6/26/12
 */
public class EndpointConfiguration {

    protected URI uri;

    public EndpointConfiguration(URI uri) {
        this.uri = uri;
    }

    public java.net.URI getURI() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EndpointConfiguration)) return false;

        EndpointConfiguration that = (EndpointConfiguration) o;

        if (!uri.equals(that.uri)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
