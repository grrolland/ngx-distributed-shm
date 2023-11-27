package io.github.grrolland.hcshm.ratelimiter;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

/**
 * RateLimiterValue TestCase
 */
public class RateLimiterShmValueTestCase {

    @Test
    public void getValue() throws InterruptedException {
        final RateLimiterShmValue rateLimiterShmValue = new RateLimiterShmValue(2, Duration.ofMillis(100));
        // Take a token
        rateLimiterShmValue.take();
        assertEquals("1", rateLimiterShmValue.getValue());
        // Wait and take another
        Thread.sleep(50);
        rateLimiterShmValue.take();
        assertEquals("0", rateLimiterShmValue.getValue());
        // Wait and getValue : the first token is expired
        Thread.sleep(50);
        assertEquals("1", rateLimiterShmValue.getValue());

        // Take another : remainng 0
        rateLimiterShmValue.take();
        assertEquals("0", rateLimiterShmValue.getValue());

        // Take another : remaning 0
        rateLimiterShmValue.take();
        assertEquals("0", rateLimiterShmValue.getValue());

        // Wait 100 : all token are expired
        Thread.sleep(100);
        assertEquals("2", rateLimiterShmValue.getValue());
    }

    @Test
    public void getRemaining() throws InterruptedException {
        final RateLimiterShmValue rateLimiterShmValue = new RateLimiterShmValue(2, Duration.ofMillis(100));
        rateLimiterShmValue.take();
        assertEquals(1, rateLimiterShmValue.getRemaining());
        // Pause and take
        Thread.sleep(51);
        rateLimiterShmValue.take();
        assertEquals(0, rateLimiterShmValue.getRemaining());

        // Pause, take and get remaining : the first token is expired
        Thread.sleep(55);
        rateLimiterShmValue.take();
        assertEquals(0, rateLimiterShmValue.getRemaining());

        // Take another
        rateLimiterShmValue.take();
        assertEquals(0, rateLimiterShmValue.getRemaining());
    }

    @Test
    public void take() {
        final RateLimiterShmValue rateLimiterShmValue = new RateLimiterShmValue(10, Duration.ofMillis(100));
        ConsumptionProbe consumptionProbe = rateLimiterShmValue.take();
        assertEquals(9, consumptionProbe.getRemainingTokens());

        consumptionProbe = rateLimiterShmValue.take();
        assertEquals(8, consumptionProbe.getRemainingTokens());

        consumptionProbe = rateLimiterShmValue.take();
        assertEquals(7, consumptionProbe.getRemainingTokens());

    }

    @Test
    public void takeAll() {
        final RateLimiterShmValue rateLimiterShmValue = new RateLimiterShmValue(2, Duration.ofMillis(100));
        ConsumptionProbe consumptionProbe = rateLimiterShmValue.take();
        assertEquals(1, consumptionProbe.getRemainingTokens());

        consumptionProbe = rateLimiterShmValue.take();
        assertEquals(0, consumptionProbe.getRemainingTokens());

        consumptionProbe = rateLimiterShmValue.take();
        assertEquals(-1, consumptionProbe.getRemainingTokens());

        consumptionProbe = rateLimiterShmValue.take();
        assertEquals(-1, consumptionProbe.getRemainingTokens());

    }

    @Test
    public void takeChangeCapacity() {
        final RateLimiterShmValue rateLimiterShmValue = new RateLimiterShmValue(3, Duration.ofMillis(100));
        assertEquals(3, rateLimiterShmValue.getRemaining());
        ConsumptionProbe consumptionProbe = rateLimiterShmValue.take();
        assertEquals(2, consumptionProbe.getRemainingTokens());
        assertEquals(2, rateLimiterShmValue.getRemaining());

        rateLimiterShmValue.setCapacity(4);
        assertEquals(3, rateLimiterShmValue.getRemaining());
        consumptionProbe = rateLimiterShmValue.take();
        assertEquals(2, consumptionProbe.getRemainingTokens());

        rateLimiterShmValue.setCapacity(3);
        assertEquals(1, rateLimiterShmValue.getRemaining());

        //  Take
        consumptionProbe = rateLimiterShmValue.take();
        assertEquals(0, consumptionProbe.getRemainingTokens());
        assertEquals(0, rateLimiterShmValue.getRemaining());

    }

    @Test
    public void takeChangeDuration() throws InterruptedException {
        final RateLimiterShmValue rateLimiterShmValue = new RateLimiterShmValue(3, Duration.ofMillis(100));
        assertEquals(3, rateLimiterShmValue.getRemaining());
        // Take token 1
        ConsumptionProbe consumptionProbe = rateLimiterShmValue.take();
        assertEquals(2, consumptionProbe.getRemainingTokens());
        // Wait
        Thread.sleep(50);

        // Change duration to 10 ms
        rateLimiterShmValue.setDuration(Duration.ofMillis(10));
        // Take token 2
        consumptionProbe = rateLimiterShmValue.take();
        // Token 1 should be expired
        assertEquals(2, consumptionProbe.getRemainingTokens());
        assertEquals(2, rateLimiterShmValue.getRemaining());

        // Change duration to 1000 ms
        rateLimiterShmValue.setDuration(Duration.ofMillis(1000));
        // Take token 3
        consumptionProbe = rateLimiterShmValue.take();

        // Wait
        Thread.sleep(500);
        // Token 2 and 3 should not be expired
        assertEquals(1, consumptionProbe.getRemainingTokens());
        assertEquals(1, rateLimiterShmValue.getRemaining());

        // Wait
        Thread.sleep(500);
        // Token 2 and 3 should be expired
        assertEquals(1, consumptionProbe.getRemainingTokens());
        assertEquals(1, rateLimiterShmValue.getRemaining());

    }

    @Test
    public void consumeTokenShouldRelease() throws InterruptedException {
        final RateLimiterShmValue rateLimiterShmValue = new RateLimiterShmValue(10, Duration.ofMillis(100));

        // Take token 1
        ConsumptionProbe consumptionProbe = rateLimiterShmValue.take();
        assertEquals(9, consumptionProbe.getRemainingTokens());
        // Wait 101 ms, token 1 should be released
        Thread.sleep(100);
        // Take token 2
        consumptionProbe = rateLimiterShmValue.take();
        assertEquals(9, consumptionProbe.getRemainingTokens());

        // Wait 50, and take token 3, token 2 should not be expired
        Thread.sleep(50);
        consumptionProbe = rateLimiterShmValue.take();
        assertEquals(8, consumptionProbe.getRemainingTokens());

        // Wait 50 and take Token 4 : token 2 should be expired, token 3 not expired
        Thread.sleep(50);
        consumptionProbe = rateLimiterShmValue.take();
        assertEquals(8, consumptionProbe.getRemainingTokens());

        // Wait 100 : token 2 and token 3 should be expired
        Thread.sleep(100);
        consumptionProbe = rateLimiterShmValue.take();
        assertEquals(9, consumptionProbe.getRemainingTokens());
    }

}
