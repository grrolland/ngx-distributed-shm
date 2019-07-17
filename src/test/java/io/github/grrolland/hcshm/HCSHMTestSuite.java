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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.LoggerFactory;

/**
 * Test Suite initializing the distributed SHM
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DeleteTestCase.class,
        GetTestCase.class,
        IncrTestCase.class,
        QuitTestCase.class,
        SetTestCase.class,
        TouchTestCase.class,
        UnknownCommandTestCase.class,
        FlushAllTestCase.class
})
public class HCSHMTestSuite {

    /**
     * Init the test case : launch the distributed memory
     */
    @BeforeClass
    public static void init() throws InterruptedException {

        System.setProperty("ngx-distributed-shm.bind_address", "0.0.0.0");
        System.setProperty("ngx-distributed-shm.port", "40321");
        System.setProperty("ngx-distributed-shm.enable_jmx_counter", "false");

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        Main.main(new String[] {});
        Thread.sleep(10000); // NOSONAR

    }

}
