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
public class TouchTestCase extends  AbstractHCSHMGetTestCase {

    /**
     * Test touching a key
     */
    @Test
    public void testTouchFound() {

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

            getWriter().write("TOUCH key 1\r\n");
            getWriter().flush();
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
            e.printStackTrace();
        }

    }

    /**
     * Test unexpiring a key by touhcing with 0
     */
    @Test
    public void testUnexpire() {

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

            getWriter().write("TOUCH key 0\r\n");
            getWriter().flush();
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

            Thread.sleep(2000); // NOSONAR

            getWriter().write("GET key\r\n");
            getWriter().flush();
            res = getReader().readLine();
            Assert.assertEquals("LEN 10", res);
            res = getReader().readLine();
            Assert.assertEquals("1234567890", res);
            res = getReader().readLine();
            Assert.assertEquals("DONE", res);

        }
        catch (IOException | InterruptedException e )
        {
            e.printStackTrace();
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
            String res = getReader().readLine();
            Assert.assertEquals("DONE", res);
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
            String res = getReader().readLine();
            Assert.assertEquals("ERROR malformed_request", res);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

}
