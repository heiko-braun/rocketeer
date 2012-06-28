package io.rocketeer;

/**
 *
 * <pre>
 *  ServerContainer serverContainer = WebSocketImplementation.getServerContainer();
 *  Endpoint helloServer = new HelloServer();
 *  ServerConfiguration serverConfig = new ServerConfiguration( new URI("/hello") );
 *  serverContainer.registerEndpoint(helloServer, serverConfig);
 * </pre>
 *
 * @author Heiko Braun
 * @date 6/26/12
 *
 */
public interface ServerContainer extends ClientContainer {
    void registerEndpoint(Endpoint endpoint, ServerConfiguration ilc);
    void start();
    void stop();
}
