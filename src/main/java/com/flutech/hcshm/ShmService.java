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



import com.flutech.hcshm.processor.IncrProcessor;
import com.flutech.hcshm.processor.TouchProcessor;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.util.Map;
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
     * Region Locator
     */
    private ShmRegionLocator regionLocator = new ShmRegionLocator();

    /**
     * Public Constructor
     * @param hazelcast the hazelcast instance
     */
    public ShmService(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }

    /**
     * Get the map form the key name
     * @param key the key
     * @return return the named IMap, if no region in the key return the default IMap
     */
    private IMap<String, ShmValue> getMap(final String key) {
        return regionLocator.getMap(hazelcast, key);
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
        getMap(key).executeOnKey(key, new TouchProcessor(expire));
    }

    /**
     * The incr operation
     * @param key the key
     * @param value the increment
     * @param init the init value
     * @return the new value as string representation
     */
    public String incr(String key, int value, int init) {
        return (String) getMap(key).executeOnKey(key, new IncrProcessor(value, init));
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
        regionLocator.getMapRegion(hazelcast, region).clear();
    }

}
