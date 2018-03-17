/*
 *
 */
package com.flutech.hcshm;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

/**
 * SHM TCP Server
 */
public class ShmTcpServer extends AbstractVerticle {

    private ShmService service = null;

    public ShmTcpServer(ShmService service) {
        this.service = service;
    }

    @Override
    public void start() throws Exception {

        NetServerOptions options = new NetServerOptions().setPort(Configuration.getPort()).setHost("127.0.0.1");
        NetServer server = vertx.createNetServer(options);
        server.connectHandler(sock -> {
            ShmProtocolHandler.create(sock, service);
        });
        server.listen();
    }

}
