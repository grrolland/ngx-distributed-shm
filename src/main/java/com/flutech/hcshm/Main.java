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

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

/**
 * Main Class
 */
public class Main {
    /**
     * Main Method
     * @param args command line arguments
     */
    public static void main(String[] args) {

        System.setProperty("vertx.disableFileCPResolving", "true");

        Config cfg = new Config();
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);

        final VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(1)
                .setMetricsOptions(
                        new DropwizardMetricsOptions().
                                setJmxEnabled(true).
                                setJmxDomain("vertx-metrics")
                );

        Vertx vertx = Vertx.vertx(vertxOptions);
        DeploymentOptions options = new DeploymentOptions().setWorker(true);
        vertx.deployVerticle(new ShmTcpServer(new ShmService(instance)), options, stringAsyncResult ->
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                /**
                 * Shutdown Hook : stop hazelcast and vertx
                 */
                @Override
                public void run()
                {
                    instance.shutdown();
                    vertx.close();
                }
            })
        );

    }
}
