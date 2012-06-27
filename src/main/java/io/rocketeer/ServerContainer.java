package io.rocketeer;

/**
 *
 * <pre>
 *  ServerContainer serverContainer = WebSocketImplementation.getServerContainer();
 *  Endpoint helloServer = new HelloServer();
 *  ServerConfiguration serverConfig = new ServerConfiguration( new URI("/hello") );
 *  serverContainer.registerServer(helloServer, serverConfig);
 * </pre>
 *
 * @author Heiko Braun
 * @date 6/26/12
 *
 */
public interface ServerContainer extends ClientContainer {
    void registerServer(Endpoint endpoint, ServerConfiguration ilc);
    void start();
    void stop();
}
