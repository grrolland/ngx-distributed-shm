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
    private IMap<String, ShmValue> shmMap;

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
        ShmValue r = shmMap.get(key);
        if (null != r) {
            return r.getValue() ;
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
        shmMap.set(key, new ShmValue(value, expire), expire, TimeUnit.SECONDS);
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
        String r =   Long.toString(value);
        shmMap.set(key, new ShmValue(r, expire), expire, TimeUnit.SECONDS);
        return r;
    }

    /**
     * The touch operation
     * @param key the key
     * @param expire the expiration in seconds
     */
    public void touch(String key, int expire) {
        shmMap.lock(key);
        final ShmValue r = shmMap.get(key);
        if (null != r) {
            r.expire(expire);
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
        final ShmValue r = shmMap.get(key);
        String newval = null;
        int expire = 0;
        if (null != r) {

            try
            {
                newval = Long.toString(Long.parseLong(r.getValue()) + value);
            }
            catch (NumberFormatException e)
            {
                newval = r.getValue();
            }
            expire = r.getLastingTime();
        }
        else
        {
            newval = Long.toString((long) value + (long) init);
        }
        if (expire >= 0) {
            shmMap.set(key, new ShmValue(newval, expire), expire, TimeUnit.SECONDS);
        }
        else
        {
            shmMap.remove(key);
        }
        shmMap.unlock(key);

        return newval;
    }

    /**
     * The delete operation
     * @param key the key to delete
     */
    public void delete(String key) {
        shmMap.delete(key);
    }
}
