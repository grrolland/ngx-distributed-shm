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
public class IncrTestCase extends AbstractHCSHMGetTestCase {

    /**
     * Test Incrementation
     */
    @Test
    public void testIncr() {

        try {
            getWriter().write("SET key 0 10\r\n");
            getWriter().write("1234567890");
            getWriter().flush();
            assertResponseGetValue("1234567890");

            getWriter().write("INCR key 1 0\r\n");
            getWriter().flush();
            assertResponseGetValue("1234567891");

            getWriter().write("GET key\r\n");
            getWriter().flush();
            assertResponseGetValue("1234567891");

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test Incrementation
     */
    @Test
    public void testIncrExpire() {

        try {
            // Increment key
            getWriter().write("INCR key -1 10 2\r\n");
            getWriter().flush();
            assertResponseGetValue("9");

            // get key
            getWriter().write("GET key\r\n");
            getWriter().flush();
            assertResponseGetValue("9");

            // Pause
            Thread.sleep(3000); // NOSONAR
            getWriter().write("GET key\r\n");
            getWriter().flush();
            assertResponseNotFound();

        } catch (IOException | InterruptedException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test Incrementation
     */
    @Test
    public void testMultiIncrExpire() {

        try {
            // Increment key
            getWriter().write("INCR key -1 10 3\r\n");
            getWriter().flush();
            assertResponseGetValue("9");

            // Pause
            pause();

            getWriter().write("INCR key -1 10 4\r\n");
            getWriter().flush();
            assertResponseGetValue("8");

            // Pause
            pause();
            getWriter().write("GET key\r\n");
            getWriter().flush();
            assertResponseNotFound();

        } catch (IOException | InterruptedException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test Incrementation
     */
    @Test
    public void testIncrExistingWithoutExpire() {

        try {
            // SET key without expire
            getWriter().write("SET key 0 2\r\n");
            getWriter().write("10");
            getWriter().flush();
            assertResponseGetValue("10");

            // INCR with expire
            getWriter().write("INCR key -1 10 2\r\n");
            getWriter().flush();
            assertResponseGetValue("9");

            getWriter().write("GET key\r\n");
            getWriter().flush();
            assertResponseGetValue("9");

            // Key should not expire
            Thread.sleep(3000); // NOSONAR

            getWriter().write("GET key\r\n");
            getWriter().flush();
            assertResponseGetValue("9");

        } catch (IOException | InterruptedException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test Incrementation
     */
    @Test
    public void testRegionIncr() {

        try {
            getWriter().write("SET region:key 0 10\r\n");
            getWriter().write("1234567890");
            getWriter().flush();
            assertResponseGetValue("1234567890");

            getWriter().write("INCR region:key 1 0\r\n");
            getWriter().flush();
            assertResponseGetValue("1234567891");

            getWriter().write("GET region:key\r\n");
            getWriter().flush();
            assertResponseGetValue("1234567891");

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test Incrementation on string
     */
    @Test
    public void testIncrOnString() {

        try {
            getWriter().write("SET key 0 10\r\n");
            getWriter().write("AZERTYUIOP");
            getWriter().flush();
            assertResponseGetValue("AZERTYUIOP");

            getWriter().write("INCR key 1 0\r\n");
            getWriter().flush();
            assertResponseGetValue("AZERTYUIOP");

            getWriter().write("GET key\r\n");
            getWriter().flush();
            assertResponseGetValue("AZERTYUIOP");

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test Incrementation on string
     */
    @Test
    public void testRegionIncrOnString() {

        try {
            getWriter().write("SET region:key 0 10\r\n");
            getWriter().write("AZERTYUIOP");
            getWriter().flush();
            assertResponseGetValue("AZERTYUIOP");

            getWriter().write("INCR region:key 1 0\r\n");
            getWriter().flush();
            assertResponseGetValue("AZERTYUIOP");

            getWriter().write("GET region:key\r\n");
            getWriter().flush();
            assertResponseGetValue("AZERTYUIOP");

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test incrementation with no set before
     */
    @Test
    public void testIncrInit() {

        try {

            getWriter().write("INCR newkey 1 10\r\n");
            getWriter().flush();
            assertResponseGetValue("11");

            getWriter().write("GET newkey\r\n");
            getWriter().flush();
            assertResponseGetValue("11");

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test incrementation with no set before
     */
    @Test
    public void testRegionIncrInit() {

        try {
            this.flushAll("region");
            getWriter().write("INCR region:newkey 1 10\r\n");
            getWriter().flush();
            assertResponseGetValue("11");

            getWriter().write("GET region:newkey\r\n");
            getWriter().flush();
            assertResponseGetValue("11");

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test getting a non existent key
     */
    @Test
    public void testIncrMalformed() {

        try {
            getWriter().write("INCR notexists bababi\r\n");
            getWriter().flush();
            assertResponseMalFormedRequest();

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

}
