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



import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * The Shared Memory Service
 */
public class ShmService {

    /**
     * The HAzelcast Instance
     */
    private HazelcastInstance hazelcast;

    /**
     * The distributed Map storing keys and values
     */
    private IMap<String, Object> shmMap;

    /**
     * Public Constructor
     * @param hazelcast the hazelcast instance
     */
    public ShmService(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
        shmMap = this.hazelcast.getMap("shmmap");
    }

    /**
     * Get Operation
     * @param key the key
     * @return the value as string or the error
     */
    public String get(String key) {
        Object r = shmMap.get(key);
        if (null != r) {
            return r instanceof Long ? r.toString() : (String) r ;
        }
        else{
            return "ERROR not_found";
        }
    }

    /**
     * The Set operation for string value
     * @param key the key
     * @param value the value as string
     * @param expire the expiration in seconds
     * @return the value set
     */
    public String set(String key, String value, int expire) {
        shmMap.set(key, value, expire, TimeUnit.SECONDS);
        return value;
    }

    /**
     * The Set operation for long value
     * @param key the key
     * @param value the value as long
     * @param expire the expiration in seconds
     * @return the value set as string representation
     */
    public String set(String key, long value, int expire) {
        Long r =   Long.valueOf(value);
        shmMap.set(key, value, expire, TimeUnit.SECONDS);
        return r.toString();
    }

    /**
     * The touch operation
     * @param key the key
     * @param expire the expiration in seconds
     */
    public void touch(String key, int expire) {
        shmMap.lock(key);
        final Object r = shmMap.get(key);
        if (null != r) {
            shmMap.set(key, r, expire, TimeUnit.SECONDS);
        }
        shmMap.unlock(key);
    }

    /**
     * The incr operation
     * @param key the key
     * @param value the increment
     * @param init the init value
     * @return the new value as string representation
     */
    public String incr(String key, int value, int init) {
        shmMap.lock(key);
        final Object r = shmMap.get(key);
        final Object newval;
        if (null != r) {
            newval = r instanceof Long ? Long.valueOf( (Long) r + value) : r;
        }
        else
        {
            newval = Long.valueOf((long) value + (long) init);
        }
        shmMap.set(key, newval);
        shmMap.unlock(key);

        return newval.toString();
    }

}
