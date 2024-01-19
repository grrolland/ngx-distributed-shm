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
package io.github.grrolland.hcshm.ratelimiter;

import io.github.grrolland.hcshm.AbstractShmValue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * RateLimiterShmValue store rate limiter data
 */
public class RateLimiterShmValue extends AbstractShmValue {

    /**
     * The number of tokens used
     */
    private final List<Token> tokens;
    /**
     * The sliding window duration
     */
    private Duration duration;
    /**
     * The capacity
     */
    private int capacity;

    /**
     * Set capacity
     *
     * @param pCapacity
     *         capacity
     */
    public void setCapacity(final int pCapacity) {
        this.capacity = pCapacity;
    }

    /**
     * Set duration
     *
     * @param pDuration
     *         duration
     */
    public void setDuration(final Duration pDuration) {
        this.duration = pDuration;
    }

    /**
     * Get the number of available tokens before capacity is exceeded
     *
     * @return the number of available tokens
     */
    public int getRemaining() {
        return Math.max(this.capacity - this.tokens.size(), 0);
    }

    @Override
    public String getValue() {
        this.clearExpired();
        return String.valueOf(getRemaining());
    }

    /**
     * Constructor
     *
     * @param capacity
     *         the capacity
     * @param duration
     *         the sliding window duration
     */
    public RateLimiterShmValue(int capacity, Duration duration) {
        this.tokens = new ArrayList<>(capacity);
        this.duration = duration;
        this.capacity = capacity;
    }

    /**
     * Try to take a token and return the ConsumptionProbe
     *
     * @return the ConsumptionProbe
     */
    public ConsumptionProbe take() {
        // Clear expired tokens
        this.clearExpired();

        int remaining = -1;
        // Try to consume
        if (this.canConsume()) {
            tokens.add(new Token());
            remaining = this.getRemaining();
        }
        return new ConsumptionProbe(remaining);
    }

    /**
     * Can consume
     *
     * @return true if at least one token is available
     */
    private boolean canConsume() {
        return this.tokens.size() < this.capacity;
    }

    /**
     * Clear expired tokens
     */
    private void clearExpired() {

        tokens.removeIf(pToken -> pToken.isExpired(this.duration));
    }
}
