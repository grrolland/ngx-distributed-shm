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
     *         the maximum count of records.
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
