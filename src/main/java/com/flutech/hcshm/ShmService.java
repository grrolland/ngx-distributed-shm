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
     * Get the map form the key name
     * @param key the key
     * @return return the named IMap, if no region in the key return the default IMap
     */
    private IMap<String, ShmValue> getMap(final String key) {
        return getMapRegion(getRegion(key));
    }

    /**
     * Get the map form the region name
     * @param region the key
     * @return return the named IMap, if no region return the default IMap
     */
    private IMap<String, ShmValue> getMapRegion(final String region) {
        if (null != region) {
            return hazelcast.getMap(region);
        }
        else {
            return hazelcast.getMap("shmmap");
        }
    }

    /**
     * Get the region from the key
     * @param key the key
     * @return the region or null if no region
     */
    private String getRegion(String key) {
        String region = null;
        final String[] splitedKey = key.split(":");
        if (splitedKey.length > 1) {
            region = splitedKey[0];
        }
        return region;
    }

    /**
     * Public Constructor
     * @param hazelcast the hazelcast instance
     */
    public ShmService(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }

    /**
     * Get Operation
     * @param key the key
     * @return the value as string or the error
     */
    public String get(String key) {
        ShmValue r = getMap(key).get(key);
        if (null != r) {
            return r.getValue() ;
        }
        else{
            return null;
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
        getMap(key).set(key, new ShmValue(value, expire), expire, TimeUnit.SECONDS);
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
        getMap(key).set(key, new ShmValue(r, expire), expire, TimeUnit.SECONDS);
        return r;
    }

    /**
     * The touch operation
     * @param key the key
     * @param expire the expiration in seconds
     */
    public void touch(String key, int expire) {
        getMap(key).lock(key);
        final ShmValue r = getMap(key).get(key);
        if (null != r) {
            r.expire(expire);
            getMap(key).set(key, r, expire, TimeUnit.SECONDS);
        }
        getMap(key).unlock(key);
    }

    /**
     * The incr operation
     * @param key the key
     * @param value the increment
     * @param init the init value
     * @return the new value as string representation
     */
    public String incr(String key, int value, int init) {
        getMap(key).lock(key);
        final ShmValue r = getMap(key).get(key);
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
            getMap(key).set(key, new ShmValue(newval, expire), expire, TimeUnit.SECONDS);
        }
        else
        {
            getMap(key).remove(key);
        }
        getMap(key).unlock(key);

        return newval;
    }

    /**
     * The delete operation
     * @param key the key to delete
     */
    public void delete(String key) {
        getMap(key).delete(key);
    }

    /**
     * Remove all entries from the region by clearing the map cluster wide
     * @param region the region name
     */
    public void flushall(String region) {
        getMapRegion(region).clear();
    }

}
