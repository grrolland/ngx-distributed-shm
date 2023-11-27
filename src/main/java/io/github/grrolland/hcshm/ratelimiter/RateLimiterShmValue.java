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
