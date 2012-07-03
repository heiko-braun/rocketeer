package io.rocketeer;

import io.rocketeer.protocol.ProtocolDef;

import javax.transaction.TransactionManager;

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

    void addProtocolSupport(ProtocolDef protocol);
    void registerEndpoint(Endpoint endpoint, ServerConfiguration ilc);
    void start();
    void stop();

    TransactionManager getTransactionManager();
}
