/**
 * ngx-distributed-shm
 * Copyright (C) 2018  Flu.Tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.flutech.hcshm;

import com.hazelcast.core.Hazelcast;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

/**
 * SHM TCP Server
 * @author grrolland
 *
 */
public class ShmTcpServer extends AbstractVerticle {

    /**
     * The Shared Memory
     * Service
     */
    private ShmService service = null;

    /**
     * Public Constructor
     */
    public ShmTcpServer() {
        this.service = new ShmService(HazelcastInstanceHandler.getInstance());
    }

    /**
     * Start the TCP Server
     * @throws Exception unable to start the server
     */
    @Override
    public void start() throws Exception {

        NetServerOptions options = new NetServerOptions().setPort(Configuration.getPort()).setHost(Configuration.getBindAddress());
        NetServer server = vertx.createNetServer(options);
        server.connectHandler(sock -> ShmProtocolHandler.create(sock, service));
        server.listen();
    }

}
