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
public class QuitTestCase extends AbstractHCSHMGetTestCase {

    /**
     * Test deleting a key
     */
    @Test
    public void testQuit() {

        try {
            getWriter().write("QUIT\r\n");
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
    public void testQuitMalformed() {

        try {
            getWriter().write("QUIT notexists bababi\r\n");
            getWriter().flush();
            assertResponseMalFormedRequest();

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

}
