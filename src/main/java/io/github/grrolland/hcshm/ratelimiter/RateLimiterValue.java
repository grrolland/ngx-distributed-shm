package io.github.grrolland.hcshm.ratelimiter;

import io.github.grrolland.hcshm.AbstractShmValue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * RateLimiterValue
 */
public class RateLimiterValue extends AbstractShmValue {

    /**
     * The current consumption records
     */
    private final List<Record> records;
    /**
     * Sliding Window duration
     */
    private final Duration duration;
    /**
     * The capacity
     */
    private final int capacity;

    /**
     * Get the remaining records before capacity is exceeded
     *
     * @return the remaining records
     */
    public int getRemaining() {
        return this.capacity - this.records.size();
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
    public RateLimiterValue(int capacity, Duration duration) {
        this.records = new ArrayList<>(capacity);
        this.duration = duration;
        this.capacity = capacity;
    }

    /**
     * Consume and return the ConsumptionProbe
     *
     * @return ConsumptionProbe
     */
    public ConsumptionProbe use() {
        // Clear expired tokens
        this.clearExpired();

        boolean consumed = false;
        int remaining = -1;
        if (canConsume()) {
            consumed = records.add(new Record());
            remaining = this.getRemaining();
        }
        return new ConsumptionProbe(consumed, remaining);
    }

    /**
     * Can consume
     *
     * @return true if at least one token is available
     */
    private boolean canConsume() {
        return this.records.size() < this.capacity;
    }

    /**
     * Clear expired tokens
     */
    private void clearExpired() {
        records.removeIf(pRecord -> pRecord.isExpired(this.duration));
    }
}
