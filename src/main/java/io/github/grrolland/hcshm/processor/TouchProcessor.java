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
package io.github.grrolland.hcshm.processor;

import io.github.grrolland.hcshm.HazelcastInstanceHandler;
import io.github.grrolland.hcshm.ShmRegionLocator;
import io.github.grrolland.hcshm.ShmValue;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Entry processor for the touch command
 */
public class TouchProcessor implements EntryProcessor<String, ShmValue>, Serializable {
    /**
     * Touch expiration
     */
    private int expire = 0;
    /**
     * Region Locator
     */
    private ShmRegionLocator regionLocator = new ShmRegionLocator();

    /**
     * constructor
     * @param expire touch expiration
     */
    public TouchProcessor(int expire) {
        this.expire = expire;
    }

    /**
     * Touch process
     * @param entry  the entry to touch
     * @return nothing
     */
    @Override
    public Object process(Map.Entry<String, ShmValue> entry) {

        final ShmValue r = entry.getValue();
        final String key = entry.getKey();
        if (null != r) {
            r.expire(expire);
            regionLocator.getMap(HazelcastInstanceHandler.getInstance(), key).set(key, r, expire, TimeUnit.SECONDS);
        }
        return null;
    }


    /**
     * The backup processor
     * @return null, because set is called in the main process
     */
    @Override
    public EntryBackupProcessor<String, ShmValue> getBackupProcessor() {
        return null;
    }
}
