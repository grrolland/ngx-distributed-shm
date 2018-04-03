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


import org.junit.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Global Protocol Test Case
 */
public class AbstractHCSHMGetTestCase {

    /**
     * Test Socket
     */
    private Socket sock = null;

    /**
     * Socket Reader
     */
    private BufferedReader reader = null;

    /**
     * Socket Writer
     */
    private BufferedWriter writer = null;

    /**
     * Init the test case : launch the distributed memory
     */
    @BeforeClass
    public static void init() throws InterruptedException {

        System.setProperty("ngx-distributed-shm.bind_address", "0.0.0.0");
        System.setProperty("ngx-distributed-shm.port", "40321");
        System.setProperty("ngx-distributed-shm.enable_jmx_counter", "false");

        Main.main(new String[] {});
        Thread.sleep(10000); // NOSONAR

    }

    /**
     * Init Socket, Reader and Writer
     */
    @Before
    public void before() {
        try {
            Socket sock = new Socket(InetAddress.getByName("localhost"), 40321);
            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Close ALL
     */
    @After
    public void after() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (sock != null) {
                sock.shutdownInput();
                sock.shutdownOutput();
                sock.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected BufferedReader getReader() {
        return reader;
    }

    protected BufferedWriter getWriter() {
        return writer;
    }

}
