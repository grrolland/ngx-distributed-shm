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
package io.github.grrolland.hcshm;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.Serializable;

/**
 * HazelCast Map Region locator
 */
public class ShmRegionLocator implements Serializable {
    /**
     * Get the map form the key name
     * @param key the key
     * @return return the named IMap, if no region in the key return the default IMap
     */
    public IMap<String, ShmValue> getMap(final HazelcastInstance hazelcast, final String key) {
        return getMapRegion(hazelcast, getRegion(key));
    }

    /**
     * Get the map form the region name
     * @param region the key
     * @return return the named IMap, if no region return the default IMap
     */
    public IMap<String, ShmValue> getMapRegion(final HazelcastInstance hazelcast, final String region) {
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
}
