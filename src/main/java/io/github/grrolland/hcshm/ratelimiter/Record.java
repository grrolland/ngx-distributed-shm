package io.github.grrolland.hcshm.ratelimiter;

import java.io.Serializable;
import java.time.Duration;

/**
 * A token with expiration date
 */
public class Record implements Serializable {

    /**
     * Expiration
     */
    private final long created;

    /**
     * Get the expiration time in milliseconds
     *
     * @return the expiration time in milliseconds
     */
    public long getCreated() {
        return created;
    }

    /**
     * Duration of the token
     */
    public Record() {
        this.created = System.currentTimeMillis();
    }

    /**
     * @param duration
     *         Duration
     * @return true if expired
     */
    boolean isExpired(Duration duration) {
        long expireAt = this.getCreated() + duration.toMillis();
        return expireAt < System.currentTimeMillis();
    }
}
