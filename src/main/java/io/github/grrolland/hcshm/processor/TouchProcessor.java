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

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Entry processor for the touch command
 */
public class TouchProcessor implements EntryProcessor<String, ShmValue, Object> {
    /**
     * Touch expiration
     */
    private final long expire;

    /**
     * constructor
     *
     * @param expire
     *         touch expiration
     */
    public TouchProcessor(long expire) {
        this.expire = expire;
    }

    private static ShmValue getCurrentValue(final Map.Entry<String, ShmValue> entry) throws BadRequestException {
        try {
            return entry.getValue();
        } catch (ClassCastException e) {
            throw new BadRequestException(e);
        }
    }

    /**
     * Touch process
     *
     * @param entry
     *         the entry to touch
     * @return nothing
     */
    @Override
    public Object process(Map.Entry<String, ShmValue> entry) {
        final ShmValue r = getCurrentValue(entry);
        if (null != r) {
            r.expire(expire);
            ((ExtendedMapEntry<String, ShmValue>) entry).setValue(r, expire, TimeUnit.MILLISECONDS);
        }
        return null;
    }
}
