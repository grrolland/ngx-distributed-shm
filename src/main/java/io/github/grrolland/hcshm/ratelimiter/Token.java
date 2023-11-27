package io.github.grrolland.hcshm.ratelimiter;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

/**
 * A token consumed at a specific date
 */
public class Token implements Serializable {

    /**
     * Expiration
     */
    private final long createdAt;

    /**
     * Constructor
     */
    public Token() {
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * @param duration
     *         Duration
     * @return true if expired
     */
    boolean isExpired(Duration duration) {
        // check if expiration date is before now
        return Instant.ofEpochMilli(this.createdAt).plus(duration).isBefore(Instant.now());
    }
}
