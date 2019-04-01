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
package com.flutech.hcshm.processor;

import com.flutech.hcshm.HazelcastInstanceHandler;
import com.flutech.hcshm.ShmRegionLocator;
import com.flutech.hcshm.ShmValue;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Entry Processor for the Incr command
 */
public class IncrProcessor implements EntryProcessor<String, ShmValue>, Serializable {
    /**
     * Incrementation value
     */
    private long value = 0;
    /**
     * Initial value
     */
    private int init = 0;
    /**
     * Region locator
     */
    private ShmRegionLocator regionLocator = new ShmRegionLocator();

    /**
     * Constructor
     * @param value incrementation value
     * @param init initial value
     */
    public IncrProcessor(long value, int init) {
        this.value= value;
        this.init = init;
    }

    /**
     * Process  the incrementation command
     * @param entry the entry to increment
     * @return the new value
     */
    @Override
    public Object process(Map.Entry<String, ShmValue> entry) {
        final ShmValue r = entry.getValue();
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
            newval = Long.toString(value + (long) init);
        }
        IMap<String, ShmValue> map = regionLocator.getMap(HazelcastInstanceHandler.getInstance(), entry.getKey());
        if (expire >= 0) {
            map.set(entry.getKey(), new ShmValue(newval, expire), expire, TimeUnit.SECONDS);
        }
        else
        {
            map.remove(entry.getKey());
        }
        return newval;
    }

    /**
     * The backup processor
     * @return null, because set or remove is called in the main process
     */
    @Override
    public EntryBackupProcessor<String, ShmValue> getBackupProcessor() {
        return null;
    }
}
