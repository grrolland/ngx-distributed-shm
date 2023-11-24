package io.github.grrolland.hcshm.processor;

import com.hazelcast.map.EntryProcessor;
import io.github.grrolland.hcshm.ratelimiter.ConsumptionProbe;
import io.github.grrolland.hcshm.ratelimiter.RateLimiterValue;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Processor for the RATE_LIMITER command
 */
public class RateLimiterProcessor implements EntryProcessor<String, RateLimiterValue, Object>, Serializable {
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
     *         the maximum count of records.
     * @param duration
     *         the sliding window duration
     */
    public RateLimiterProcessor(int capacity, Duration duration) {
        this.capacity = capacity;
        this.duration = duration;
    }

    @Override
    public ConsumptionProbe process(final Map.Entry<String, RateLimiterValue> entry) {
        RateLimiterValue rateLimiterValue = Optional.ofNullable(getCurrentValue(entry)).orElseGet(this::create);
        final ConsumptionProbe consumptionProbe = rateLimiterValue.use();
        entry.setValue(rateLimiterValue);
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
    private RateLimiterValue getCurrentValue(final Map.Entry<String, RateLimiterValue> entry) throws BadRequestException {
        try {
            return entry.getValue();
        } catch (ClassCastException e) {
            throw new BadRequestException(e);
        }
    }

    private RateLimiterValue create() {
        return new RateLimiterValue(this.capacity, this.duration);
    }

}
