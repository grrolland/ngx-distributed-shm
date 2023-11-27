package io.github.grrolland.hcshm.ratelimiter;

/**
 * Describes token consumed, and number of tokens remaining
 * <p>
 * remainingTokens -1 means no tokens have been consumed
 * remainingTokens 0 means there is no more tokens to consume
 */
public class ConsumptionProbe {

    /**
     * Number of remaining tokens
     */
    private final int remainingTokens;

    /**
     * Return the number of remaining available tokens or -1 if capacity was exceeded
     *
     * @return the number of remaining available tokens or -1
     */
    public int getRemainingTokens() {
        return this.remainingTokens;

    }

    /**
     * Constructor
     *
     * @param remainingTokens
     *         the number of remaining token
     */
    ConsumptionProbe(int remainingTokens) {
        this.remainingTokens = remainingTokens;
    }
}
