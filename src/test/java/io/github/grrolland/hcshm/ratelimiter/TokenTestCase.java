package io.github.grrolland.hcshm.ratelimiter;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/***
 * Token test case
 */
public class TokenTestCase {

    @Test
    public void isExpired() throws InterruptedException {
        Token token = new Token();
        assertTrue(token.isExpired(Duration.ofMillis(0)));
        Thread.sleep(10);
        assertTrue(token.isExpired(Duration.ofMillis(500)));
    }

    @Test
    public void isExpiredFalse() throws InterruptedException {
        Token token = new Token();
        assertFalse(token.isExpired(Duration.ofMillis(100)));
        Thread.sleep(50);
        assertFalse(token.isExpired(Duration.ofMillis(100)));
    }
}
