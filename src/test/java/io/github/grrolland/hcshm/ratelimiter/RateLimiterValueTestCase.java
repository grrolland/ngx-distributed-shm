package io.github.grrolland.hcshm.ratelimiter;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

/**
 * RateLimiterValue TestCase
 */
public class RateLimiterValueTestCase {

    @Test
    public void getRemaining() {
        final RateLimiterValue rateLimiterValue = new RateLimiterValue(2, Duration.ofMillis(1000));
        rateLimiterValue.use();
        assertEquals(1, rateLimiterValue.getRemaining());
        rateLimiterValue.use();
        assertEquals(0, rateLimiterValue.getRemaining());
        rateLimiterValue.use();
        assertEquals(0, rateLimiterValue.getRemaining());
    }

    @Test
    public void consumeToken() {
        final RateLimiterValue rateLimiterValue = new RateLimiterValue(10, Duration.ofMillis(1000));
        ConsumptionProbe consumptionProbe = rateLimiterValue.use();
        assertEquals(9, consumptionProbe.getRemainingTokens());

        consumptionProbe = rateLimiterValue.use();
        assertEquals(8, consumptionProbe.getRemainingTokens());

        consumptionProbe = rateLimiterValue.use();
        assertEquals(7, consumptionProbe.getRemainingTokens());

    }

    @Test
    public void consumeAllToken() {
        final RateLimiterValue rateLimiterValue = new RateLimiterValue(2, Duration.ofMillis(1000));
        ConsumptionProbe consumptionProbe = rateLimiterValue.use();
        assertEquals(1, consumptionProbe.getRemainingTokens());

        consumptionProbe = rateLimiterValue.use();
        assertEquals(0, consumptionProbe.getRemainingTokens());

        consumptionProbe = rateLimiterValue.use();
        assertEquals(-1, consumptionProbe.getRemainingTokens());

        consumptionProbe = rateLimiterValue.use();
        assertEquals(-1, consumptionProbe.getRemainingTokens());

    }

    @Test
    public void consumeTokenShouldRelease() throws InterruptedException {
        final RateLimiterValue rateLimiterValue = new RateLimiterValue(10, Duration.ofMillis(100));

        // Consumer token 1
        ConsumptionProbe consumptionProbe = rateLimiterValue.use();
        assertEquals(9, consumptionProbe.getRemainingTokens());
        // Wait 101 ms, token 1 should be released
        Thread.sleep(101);
        // token2
        consumptionProbe = rateLimiterValue.use();
        assertEquals(9, consumptionProbe.getRemainingTokens());

        // Wait 50, and consume, token 2 should not be expired
        Thread.sleep(50);
        consumptionProbe = rateLimiterValue.use();
        assertEquals(8, consumptionProbe.getRemainingTokens());

        // Wait 51 : token2 should be expired
        Thread.sleep(51);
        consumptionProbe = rateLimiterValue.use();
        assertEquals(8, consumptionProbe.getRemainingTokens());

        // Wait 51 : token2 should be expired
        Thread.sleep(101);
        consumptionProbe = rateLimiterValue.use();
        assertEquals(9, consumptionProbe.getRemainingTokens());
    }

}
