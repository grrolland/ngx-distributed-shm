/**
 * ngx-distributed-shm
 * Copyright (C) 2018  Flu.Tech
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.grrolland.hcshm;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Global Protocol Test Case
 */
public class TouchTestCase extends AbstractHCSHMGetTestCase {

    /**
     * Test touching a key
     */
    @Test
    public void testTouchFound() {

        try {
            getWriter().write("SET key 0 10\r\n");
            getWriter().write("1234567890");
            getWriter().flush();
            assertResponseGetValue("1234567890");

            getWriter().write("TOUCH key 1\r\n");
            getWriter().flush();
            assertResponseDone();

            pause();

            getWriter().write("GET key\r\n");
            getWriter().flush();
            assertResponseNotFound();

        } catch (IOException | InterruptedException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test unexpiring a key by touhcing with 0
     */
    @Test
    public void testUnexpire() {

        try {
            // Ensure key is not found (debug test)
            getWriter().write("GET key\r\n");
            getWriter().flush();
            assertResponseNotFound();
            // Set key that expire in 3 s
            getWriter().write("SET key 3 10\r\n");
            getWriter().write("1234567890");
            getWriter().flush();
            assertResponseGetValue("1234567890");

            // Clear key expiration
            getWriter().write("TOUCH key 0\r\n");
            getWriter().flush();
            assertResponseDone();

            // Wait more than 3 seconds
            pause(4000);

            // Key should be there
            getWriter().write("GET key\r\n");
            getWriter().flush();
            assertResponseGetValue("1234567890");

        } catch (IOException | InterruptedException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test touching a non existent key
     */
    @Test
    public void testTouchNotFound() {

        try {
            getWriter().write("TOUCH notexists 1\r\n");
            getWriter().flush();
            assertResponseDone();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test getting a non existent key
     */
    @Test
    public void testTouchMalformed() {

        try {
            getWriter().write("Touch notexists bababi\r\n");
            getWriter().flush();
            assertResponseMalFormedRequest();

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

}
