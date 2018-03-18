package com.flutech.hcshm;

public class Configuration {

    public static int getPort() {
        try
        {
            return Integer.parseInt(System.getProperty("ngx-distributed-shm.port", "4321"));
        }
        catch (NumberFormatException e)
        {
            return 4321;
        }

    }

    public static String getBindAddress() {
        return System.getProperty("ngx-distributed-shm.bind_address", "127.0.0.1");
    }

}
