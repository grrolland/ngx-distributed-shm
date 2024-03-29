/**
 * ngx-distributed-shm
 * Copyright (C) 2018  Flu.Tech
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.grrolland.hcshm.processor;

import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.ExtendedMapEntry;
import io.github.grrolland.hcshm.ShmValue;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Entry Processor for the Incr command
 */
public class IncrProcessor implements EntryProcessor<String, ShmValue, Object>, Serializable {
    /**
     * Incrementation value
     */
    private final long value;

    /**
     * Initial value
     */
    private final int init;

    /**
     * Initial TTL value in milliseconds
     */
    private final long initialExpire;

    /**
     * Constructor
     *
     * @param value
     *         incrementation value
     * @param init
     *         initial value
     * @param initialExpire
     *         the initial expiration in milliseconds
     */
    public IncrProcessor(long value, int init, long initialExpire) {
        this.value = value;
        this.init = init;
        this.initialExpire = initialExpire;
    }

    private static ShmValue getCurrentValue(final Map.Entry<String, ShmValue> entry) throws BadRequestException {
        try {
            return entry.getValue();
        } catch (ClassCastException e) {
            throw new BadRequestException(e);
        }
    }

    /**
     * Process  the incrementation command
     *
     * @param entry
     *         the entry to increment
     * @return the new value
     */
    @Override
    public Object process(Map.Entry<String, ShmValue> entry) {

        final ShmValue r = getCurrentValue(entry);
        String newval;
        long expire;
        ExtendedMapEntry<String, ShmValue> extendedMapEntry = (ExtendedMapEntry<String, ShmValue>) entry;
        if (null != r) {
            try {
                newval = Long.toString(Long.parseLong(r.getValue()) + value);
            } catch (NumberFormatException e) {
                newval = r.getValue();
            }
            expire = r.getLastingTime();
        } else {
            newval = Long.toString(value + init);
            expire = this.initialExpire;
        }
        if (expire >= 0) {
            extendedMapEntry.setValue(new ShmValue(newval, expire), expire, TimeUnit.MILLISECONDS);
        } else {
            extendedMapEntry.setValue(null);
        }
        return newval;
    }

}
