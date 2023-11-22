/**
 * ngx-distributed-shm
 * Copyright (C) 2018  Flu.Tech
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.grrolland.hcshm;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

/**
 * SHM TCP Server
 *
 * @author grrolland
 */
public class ShmTcpServer extends AbstractVerticle {

    /**
     * The Shared Memory
     * Service
     */
    private final ShmService service;

    /**
     * Public Constructor
     */
    public ShmTcpServer() {
        this.service = new ShmService(HazelcastInstanceHandler.getInstance());
    }

    /**
     * Start the TCP Server
     */
    @Override
    public void start() {
        NetServerOptions options = new NetServerOptions().setPort(Configuration.getPort()).setHost(Configuration.getBindAddress());
        NetServer server = vertx.createNetServer(options);
        server.connectHandler(sock -> ShmProtocolHandler.create(sock, service));
        server.listen();
    }

}
