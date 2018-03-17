package com.flutech.hcshm;



import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;


public class ShmService implements Serializable {

    private HazelcastInstance hazelcast;

    private IMap<String, Object> shmMap;

    public ShmService(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
        shmMap = this.hazelcast.getMap("shmmap");
    }

    public String get(String key) {
        Object r = shmMap.get(key);
        if (null != r) {
            return r instanceof Long ? r.toString() : (String) r ;
        }
        else{
            return "NOT FOUND";
        }
    }

    public String set(String key, String value, int expire) {
        shmMap.set(key, value, expire, TimeUnit.SECONDS);
        return value;
    }

    public String set(String key, long value, int expire) {
        Long r =   Long.valueOf(value);
        shmMap.set(key, value, expire, TimeUnit.SECONDS);
        return r.toString();
    }

    public void touch(String key, int expire) {
        shmMap.lock(key);
        final Object r = shmMap.get(key);
        if (null != r) {
            shmMap.set(key, r, expire, TimeUnit.SECONDS);
        }
        shmMap.unlock(key);
    }

    public String incr(String key, int value, int init) {
        shmMap.lock(key);
        final Object r = shmMap.get(key);
        final Object newval;
        if (null != r) {
            newval = r instanceof Long ? Long.valueOf( (Long) r + value) : r;
        }
        else
        {
            newval = Long.valueOf(value + init);
        }
        shmMap.set(key, newval);
        shmMap.unlock(key);

        return newval.toString();
    }

}
