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
package io.github.grrolland.hcshm;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.grrolland.hcshm.processor.IncrProcessor;
import io.github.grrolland.hcshm.processor.RateLimiterProcessor;
import io.github.grrolland.hcshm.processor.TouchProcessor;
import io.github.grrolland.hcshm.ratelimiter.ConsumptionProbe;
import io.github.grrolland.hcshm.ratelimiter.RateLimiterValue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * The Shared Memory Service
 */
public class ShmService {

    /**
     * The HAzelcast Instance
     */
    private final HazelcastInstance hazelcast;

    /**
     * Region Locator
     */
    private final ShmRegionLocator regionLocator = new ShmRegionLocator();

    /**
     * Public Constructor
     *
     * @param hazelcast
     *         the hazelcast instance
     */
    public ShmService(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }

    /**
     * Get Operation
     *
     * @param key
     *         the key
     * @return the value as string or the error
     */
    public String get(String key) {
        IMap<String, AbstractShmValue> map = getMap(key);
        AbstractShmValue r = map.get(key);
        if (null != r) {
            return r.getValue();
        } else {
            return null;
        }
    }

    /**
     * The Set operation for string value
     *
     * @param key
     *         the key
     * @param value
     *         the value as string
     * @param expire
     *         the expiration in milliseconds
     * @return the value set
     */
    public String set(String key, String value, long expire) {
        getMap(key).set(key, new ShmValue(value, expire), expire, TimeUnit.MILLISECONDS);
        return value;
    }

    /**
     * The Set operation for long value
     *
     * @param key
     *         the key
     * @param value
     *         the value as long
     * @param expire
     *         the expiration in milliseconds
     * @return the value set as string representation
     */
    public String set(String key, long value, long expire) {
        String r = Long.toString(value);
        getMap(key).set(key, new ShmValue(r, expire), expire, TimeUnit.MILLISECONDS);
        return r;
    }

    /**
     * The touch operation
     *
     * @param key
     *         the key
     * @param expire
     *         the expiration in milliseconds
     */
    public void touch(String key, long expire) {
        IMap<String, ShmValue> map = getMap(key);
        map.executeOnKey(key, new TouchProcessor(expire));
    }

    /**
     * The incr operation
     *
     * @param key
     *         the key
     * @param value
     *         the increment
     * @param init
     *         the init value
     * @param initialExpire
     *         the initial expiration
     * @return the new value as string representation
     */
    public String incr(String key, int value, int init, long initialExpire) {
        IMap<String, ShmValue> map = getMap(key);
        return (String) map.executeOnKey(key, new IncrProcessor(value, init, initialExpire));
    }

    /**
     * The delete operation
     *
     * @param key
     *         the key to delete
     */
    public void delete(String key) {
        getMap(key).delete(key);
    }

    /**
     * Remove all entries from the region by clearing the map cluster wide
     *
     * @param region
     *         the region name
     */
    public void flushall(String region) {
        regionLocator.getMapRegion(hazelcast, region).clear();
    }

    /***
     * Consume a token
     * @param key  the key
     * @param capacity  the maximum capacity
     * @param duration the duration of a token
     * @return the number of tokens remaining
     */
    public String rateLimiter(String key, int capacity, long duration) {
        final IMap<String, RateLimiterValue> map = regionLocator.getMap(hazelcast, key);
        RateLimiterProcessor rateLimiterProcessor = new RateLimiterProcessor(capacity, Duration.ofMillis(duration));
        ConsumptionProbe consumptionProbe = (ConsumptionProbe) map.executeOnKey(key, rateLimiterProcessor);
        return String.valueOf(consumptionProbe.getRemainingTokens());
    }

    /**
     * Get the map form the key name
     *
     * @param key
     *         the key
     * @return return the named IMap, if no region in the key return the default IMap
     */
    private <T> IMap<String, T> getMap(final String key) {
        return regionLocator.getMap(hazelcast, key);
    }

}
