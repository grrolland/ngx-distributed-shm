/*
 *
 */
package com.flutech.hcshm;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

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

        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(40));
        DeploymentOptions options = new DeploymentOptions().setWorker(true);
        vertx.deployVerticle(new ShmTcpServer(new ShmService(instance)), stringAsyncResult -> {
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                public void run()
                {
                    System.out.println("Closing vertx...");
                    instance.shutdown();
                    vertx.close(closeResult -> System.out.println("Done ."));
                }
            });

        });

    }
}
