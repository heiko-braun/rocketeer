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

        int port = 9002;

        if(args.length > 0)
            port = Integer.valueOf(args[0]);

        ServerContainer server = Rocketeer.createServer(port);
        server.registerEndpoint(new EchoEndpoint(), new ServerConfiguration(new URI("/echo"))
        );
        server.start();
    }
}
