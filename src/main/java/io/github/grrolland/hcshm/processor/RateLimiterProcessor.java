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
import io.github.grrolland.hcshm.ratelimiter.ConsumptionProbe;
import io.github.grrolland.hcshm.ratelimiter.RateLimiterShmValue;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Processor for the RATE_LIMITER command
 */
public class RateLimiterProcessor implements EntryProcessor<String, RateLimiterShmValue, Object>, Serializable {
    /**
     * Capacity
     */
    private final int capacity;

    /**
     * Sliding window duration
     */
    private final Duration duration;

    /**
     * Constructor
     *
     * @param capacity
     *         the maximum capacity of the rate limiter
     * @param duration
     *         the sliding window duration
     */
    public RateLimiterProcessor(int capacity, Duration duration) {
        this.capacity = capacity;
        this.duration = duration;
    }

    @Override
    public ConsumptionProbe process(final Map.Entry<String, RateLimiterShmValue> entry) {
        RateLimiterShmValue rateLimiterShmValue = Optional.ofNullable(getCurrentValue(entry)).orElseGet(this::create);
        rateLimiterShmValue.setDuration(this.duration);
        rateLimiterShmValue.setCapacity(this.capacity);
        final ConsumptionProbe consumptionProbe = rateLimiterShmValue.take();
        entry.setValue(rateLimiterShmValue);
        return consumptionProbe;
    }

    /**
     * Get the current value
     *
     * @param entry
     *         the entry
     * @return the current value
     * @throws BadRequestException
     *         exception
     */
    private RateLimiterShmValue getCurrentValue(final Map.Entry<String, RateLimiterShmValue> entry) throws BadRequestException {
        try {
            return entry.getValue();
        } catch (ClassCastException e) {
            throw new BadRequestException(e);
        }
    }

    private RateLimiterShmValue create() {
        return new RateLimiterShmValue(this.capacity, this.duration);
    }

}
