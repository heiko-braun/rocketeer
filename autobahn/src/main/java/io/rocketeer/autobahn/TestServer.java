package io.rocketeer.autobahn;

import io.rocketeer.Rocketeer;
import io.rocketeer.ServerConfiguration;
import io.rocketeer.ServerContainer;

import java.net.URI;


/**
 * @author Heiko Braun
 * @date 7/3/12
 */
public class TestServer {
    public static void main(String[] args) throws Exception
    {
        if(args.length == 0)
            throw new IllegalArgumentException("Port number argument is missing");

        ServerContainer server = Rocketeer.createServer(Integer.valueOf(args[0]));
        server.registerEndpoint(new EchoEndpoint(), new ServerConfiguration(new URI("/echo"))
        );
        server.start();
    }
}
