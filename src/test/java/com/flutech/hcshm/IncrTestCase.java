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
package com.flutech.hcshm;


import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Global Protocol Test Case
 */
public class IncrTestCase extends  AbstractHCSHMGetTestCase {
 
    /**
     * Test Incrementation
     */
    @Test
    public void testIncr() {

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

            getWriter().write("INCR key 1 0\r\n");
            getWriter().flush();
            res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("1234567891", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

            getWriter().write("GET key\r\n");
            getWriter().flush();
            res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("1234567891", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

        }
        catch (IOException e)
        {
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
            String res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("1234567890", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

            getWriter().write("INCR region:key 1 0\r\n");
            getWriter().flush();
            res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("1234567891", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

            getWriter().write("GET region:key\r\n");
            getWriter().flush();
            res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("1234567891", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

        }
        catch (IOException e)
        {
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
            String res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("AZERTYUIOP", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

            getWriter().write("INCR key 1 0\r\n");
            getWriter().flush();
            res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("AZERTYUIOP", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

            getWriter().write("GET key\r\n");
            getWriter().flush();
            res = getReader().readLine();
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
     * Test Incrementation on string
     */
    @Test
    public void testRegionIncrOnString() {

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

            getWriter().write("INCR region:key 1 0\r\n");
            getWriter().flush();
            res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("AZERTYUIOP", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

            getWriter().write("GET region:key\r\n");
            getWriter().flush();
            res = getReader().readLine();
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
     * Test incrementation with no set before
     */
    @Test
    public void testIncrInit() {

        try {

            getWriter().write("INCR newkey 1 10\r\n");
            getWriter().flush();
            String res = getReader().readLine();
            Assert.assertEquals("LEN 2", res);
            res = getReader().readLine();
            Assert.assertEquals("11", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

            getWriter().write("GET newkey\r\n");
            getWriter().flush();
            res = getReader().readLine();
            Assert.assertEquals("LEN 2", res);
            res = getReader().readLine();
            Assert.assertEquals("11", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test incrementation with no set before
     */
    @Test
    public void testRegionIncrInit() {

        try {

            getWriter().write("INCR region:newkey 1 10\r\n");
            getWriter().flush();
            String res = getReader().readLine();
            Assert.assertEquals("LEN 2", res);
            res = getReader().readLine();
            Assert.assertEquals("11", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

            getWriter().write("GET region:newkey\r\n");
            getWriter().flush();
            res = getReader().readLine();
            Assert.assertEquals("LEN 2", res);
            res = getReader().readLine();
            Assert.assertEquals("11", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

        }
        catch (IOException e)
        {
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
            String res = getReader().readLine();
            Assert.assertEquals("ERROR malformed_request", res);

        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }

    }

}
