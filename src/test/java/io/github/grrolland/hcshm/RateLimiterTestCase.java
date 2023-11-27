package io.github.grrolland.hcshm;

import org.junit.Test;

import java.io.IOException;

public class RateLimiterTestCase extends AbstractHCSHMGetTestCase {

    /**
     * Test Incrementation
     */
    @Test
    public void testConsume() throws IOException, InterruptedException {

        getWriter().write("RATE_LIMITER ratekey 10 1\r\n");
        getWriter().flush();
        assertResponseGetValue("9");

        getWriter().write("RATE_LIMITER ratekey 10 1\r\n");
        getWriter().flush();
        assertResponseGetValue("8");

        pause(2000);

        getWriter().write("RATE_LIMITER ratekey 10 1\r\n");
        getWriter().flush();
        assertResponseGetValue("9");

    }

    @Test
    public void testConsumeAll() throws IOException, InterruptedException {

        for (int i = 0; i < 10; i++) {
            getWriter().write("RATE_LIMITER ratekey 10 2\r\n");
            getWriter().flush();
            assertResponseGetValue(String.valueOf(10 - 1 - i));
        }

        getWriter().write("RATE_LIMITER ratekey 10 1\r\n");
        getWriter().flush();
        assertResponseGetValue("-1");
        pause(3000);

        getWriter().write("RATE_LIMITER ratekey 10 2\r\n");
        getWriter().flush();
        assertResponseGetValue("9");

    }
}
