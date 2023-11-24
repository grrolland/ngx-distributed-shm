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

        pause(1000);

        getWriter().write("RATE_LIMITER ratekey 10 1\r\n");
        getWriter().flush();
        assertResponseGetValue("9");

    }

    @Test
    public void testConsumeAll() throws IOException {

        for (int i = 0; i < 10; i++) {
            getWriter().write("RATE_LIMITER ratekey 10 500\r\n");
            getWriter().flush();
            assertResponseGetValue(String.valueOf(10 - 1 - i));
        }

        getWriter().write("RATE_LIMITER ratekey 10 500\r\n");
        getWriter().flush();
        assertResponseGetValue("-1");

    }
}
