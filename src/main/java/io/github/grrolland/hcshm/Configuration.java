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

/**
 * Configuration Class
 * <p>
 * Get system properties for configuring server
 *
 * @author grrolland
 */
public class Configuration {
    /**
     * Default Bind Port
     */
    public static final int DEFAULT_PORT = 4321;
    /**
     * Default Bind Address
     */
    public static final String DEFAULT_BIND_ADDRESS = "localhost";
    /**
     * Default workers
     */
    public static final int DEFAULT_WORKERS = 1;

    /**
     * Get the bind port of the server with the ngx-distributed-shm.port system property
     *
     * @return the bind port (4321 by default)
     */
    public static int getPort() {
        try {
            return Integer.parseInt(System.getProperty("ngx-distributed-shm.port", Integer.toString(DEFAULT_PORT)));
        } catch (NumberFormatException e) {
            return DEFAULT_PORT;
        }
    }

    /**
     * Get the bind port of the server with the ngx-distributed-shm.port system property
     *
     * @return the bind port (4321 by default)
     */
    public static int getWorkers() {
        try {
            return Integer.parseInt(System.getProperty("ngx-distributed-shm.workers", Integer.toString(DEFAULT_WORKERS)));
        } catch (NumberFormatException e) {
            return DEFAULT_WORKERS;
        }
    }

    /**
     * Get the bind port of the server with the ngx-distributed-shm.port system property
     *
     * @return the bind port (4321 by default)
     */
    public static boolean getEnableJMXCounter() {
        return Boolean.parseBoolean(System.getProperty("ngx-distributed-shm.enable_jmx_counter", Boolean.toString(true)));
    }

    /**
     * Get the bind address with the ngx-distributed-shm.bind_address system property
     *
     * @return return th bind address (127.0.0.1 by default)
     */
    public static String getBindAddress() {
        return System.getProperty("ngx-distributed-shm.bind_address", DEFAULT_BIND_ADDRESS);
    }

    /**
     * Private default constructor
     */
    private Configuration() {
    }

}
