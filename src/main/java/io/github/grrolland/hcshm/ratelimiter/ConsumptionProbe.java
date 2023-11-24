package io.github.grrolland.hcshm.ratelimiter;

/**
 * Describes token consumed, and tokens remaining
 */
public class ConsumptionProbe {
    /**
     * Token has been consumed
     */
    private final boolean consumed;

    /**
     * Number of remaining tokens
     */
    private final int remainingTokens;

    /**
     * return the flag consumed
     *
     * @return true if token was consumed
     */
    public boolean isConsumed() {
        return this.consumed;
    }

    /**
     * Return the remaining records
     *
     * @return number of records available
     */
    public int getRemainingTokens() {
        return this.remainingTokens;

    }

    /**
     * Constructor
     *
     * @param consumed
     *         true if token has been consumer
     * @param remainingTokens
     *         the number of remaining token
     */
    ConsumptionProbe(boolean consumed, int remainingTokens) {
        this.consumed = consumed;
        this.remainingTokens = remainingTokens;
    }
}
