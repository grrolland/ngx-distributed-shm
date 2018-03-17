package com.flutech.hcshm;

public class Configuration {

    public static int getPort() {
        try
        {
            return Integer.parseInt(System.getProperty("hazelcast-shm.port", "4321"));
        }
        catch (NumberFormatException e)
        {
            return 4321;
        }

    }


}
