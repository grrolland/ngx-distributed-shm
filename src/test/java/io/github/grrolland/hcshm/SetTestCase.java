/**
 * ngx-distributed-shm
 * Copyright (C) 2018  Flu.Tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
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
public class SetTestCase extends  AbstractHCSHMGetTestCase {

    /**
     * Test Setting a string with no expiration
     */
    @Test
    public void testSetStringNoExpire() {

        try {
            getWriter().write("SET key 0 10\r\n");
            getWriter().write("AZERTYUIOP");
            getWriter().flush();
            String res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("AZERTYUIOP", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test Setting a string with no expiration
     */
    @Test
    public void testRegionSetStringNoExpire() {

        try {
            getWriter().write("SET region:key 0 10\r\n");
            getWriter().write("AZERTYUIOP");
            getWriter().flush();
            String res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("AZERTYUIOP", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test setting an integer with no expiration
     */
    @Test
    public void testSetIntNoExpire() {

        try {
            getWriter().write("SET key 0 10\r\n");
            getWriter().write("1234567890");
            getWriter().flush();
            String res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("1234567890", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test setting an integer with no expiration
     */
    @Test
    public void testRegionSetIntNoExpire() {

        try {
            getWriter().write("SET region:key 0 10\r\n");
            getWriter().write("1234567890");
            getWriter().flush();
            String res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("1234567890", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test a malformed request
     */
    @Test
    public void testSetMalformed() {

        try {
            getWriter().write("SET key 0 0 10\r\n");
            getWriter().write("1234567890");
            getWriter().flush();
            String res = getReader().readLine();
            Assert.assertEquals("ERROR malformed_request", res);
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test setting an integer with expiration
     */
    @Test
    public void testSetIntExpire() {

        try {
            getWriter().write("SET key 1 10\r\n");
            getWriter().write("1234567890");
            getWriter().flush();
            String res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("1234567890", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

            Thread.sleep(2000); // NOSONAR

            getWriter().write("GET key\r\n");
            getWriter().flush();
            res = getReader().readLine();
            Assert.assertEquals("ERROR not_found", res);

        }
        catch (IOException | InterruptedException e )
        {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test setting an integer with expiration
     */
    @Test
    public void testRegionSetIntExpire() {

        try {
            getWriter().write("SET region:key 1 10\r\n");
            getWriter().write("1234567890");
            getWriter().flush();
            String res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("1234567890", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

            Thread.sleep(2000); // NOSONAR

            getWriter().write("GET region:key\r\n");
            getWriter().flush();
            res = getReader().readLine();
            Assert.assertEquals("ERROR not_found", res);

        }
        catch (IOException | InterruptedException e )
        {
            Assert.fail(e.getMessage());
        }

    }


}
