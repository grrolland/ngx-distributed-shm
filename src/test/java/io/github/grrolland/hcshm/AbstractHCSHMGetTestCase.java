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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Global Protocol Test Case
 */
public abstract class AbstractHCSHMGetTestCase {

    /**
     * Test Socket
     */
    private final Socket sock = null;

    /**
     * Socket Reader
     */
    private BufferedReader reader = null;

    /**
     * Socket Writer
     */
    private BufferedWriter writer = null;

    protected BufferedReader getReader() {
        return reader;
    }

    protected BufferedWriter getWriter() {
        return writer;
    }

    /**
     * Init Socket, Reader and Writer
     */
    @Before
    public void before() {
        try {
            Socket sock = new Socket(InetAddress.getByName("localhost"), 4321);
            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Flush default region
     *
     * @throws IOException
     *         I/O exception
     */
    public void flush() throws IOException {
        this.flush("");
    }

    /**
     * Flush ALL region
     *
     * @param region
     *         the region
     * @throws IOException
     *         I/O exception
     */
    public void flush(String region) throws IOException {
        getWriter().write(String.format("FLUSHALL %s\r\n", region));
        getWriter().flush();
        getReader().readLine();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Assert a value is read
     *
     * @param expect
     *         expected value
     * @throws IOException
     *         I/O Exception
     */
    public void assertGetValue(String expect) throws IOException {
        String res = getReader().readLine();
        Assert.assertEquals("LEN " + expect.length(), res);
        res = getReader().readLine();
        Assert.assertEquals(expect, res);
        res = getReader().readLine();
        Assert.assertEquals("DONE", res);
    }
}
