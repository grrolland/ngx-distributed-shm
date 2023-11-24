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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.LoggerFactory;

/**
 * Test Suite initializing the distributed SHM
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ShmValueTestCase.class,
        DeleteTestCase.class,
        GetTestCase.class,
        IncrTestCase.class,
        QuitTestCase.class,
        SetTestCase.class,
        TouchTestCase.class,
        UnknownCommandTestCase.class,
        FlushAllTestCase.class})
public class HCSHMTestSuite {

    private static final Thread SHM_THREAD = new Thread(() -> Main.main(new String[]{}));

    /**
     * Init the test case : launch the distributed memory
     */
    @BeforeClass
    public static void init() {

        System.setProperty("ngx-distributed-shm.bind_address", "0.0.0.0");
        System.setProperty("ngx-distributed-shm.port", "40321");
        System.setProperty("ngx-distributed-shm.enable_jmx_counter", "false");

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        SHM_THREAD.start();
    }

    @AfterClass
    public static void shutdown() throws InterruptedException {
        SHM_THREAD.interrupt();
        SHM_THREAD.join();
    }

}
