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
public class HcshmTestCase {

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
    public static void init() {

        Main.main(new String[] {});

    }

    /**
     * Init Socket, Reader and Writer
     */
    @Before
    public void before() {
        try {
            Socket sock = new Socket(InetAddress.getByName("127.0.0.1"), 4321);
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

    /**
     * Test Setting a string with no expiration
     */
    @Test
    public void testSetStringNoExpire() {

        try {
            writer.write("SET key 0 10\r\n");
            writer.write("AZERTYUIOP");
            writer.flush();
            String res = reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("AZERTYUIOP", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Test Getting a String
     */
    @Test
    public void testGetString() {

        try {
            writer.write("SET key 0 10\r\n");
            writer.write("AZERTYUIOP");
            writer.flush();
            String res = reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("AZERTYUIOP", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

            writer.write("GET key\r\n");
            writer.flush();
            res = reader.readLine();
            Assert.assertEquals("LEN 10", res);
            res = reader.readLine();
            Assert.assertEquals("AZERTYUIOP", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Test Getting an Integer
     */
    @Test
    public void testGetInt() {

        try {
            writer.write("SET key 0 10\r\n");
            writer.write("1234567890");
            writer.flush();
            String res = reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("1234567890", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

            writer.write("GET key\r\n");
            writer.flush();
            res = reader.readLine();
            Assert.assertEquals("LEN 10", res);
            res = reader.readLine();
            Assert.assertEquals("1234567890", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Test Incrementation
     */
    @Test
    public void testIncr() {

        try {
            writer.write("SET key 0 10\r\n");
            writer.write("1234567890");
            writer.flush();
            String res = reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("1234567890", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

            writer.write("INCR key 1 0\r\n");
            writer.flush();
            res = reader.readLine();
            Assert.assertEquals("LEN 10", res);
            res = reader.readLine();
            Assert.assertEquals("1234567891", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

            writer.write("GET key\r\n");
            writer.flush();
            res = reader.readLine();
            Assert.assertEquals("LEN 10", res);
            res = reader.readLine();
            Assert.assertEquals("1234567891", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Test incrementation with no set before
     */
    @Test
    public void testIncrInit() {

        try {

            writer.write("INCR newkey 1 10\r\n");
            writer.flush();
            String res = reader.readLine();
            Assert.assertEquals("LEN 2", res);
            res = reader.readLine();
            Assert.assertEquals("11", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

            writer.write("GET newkey\r\n");
            writer.flush();
            res = reader.readLine();
            Assert.assertEquals("LEN 2", res);
            res = reader.readLine();
            Assert.assertEquals("11", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Test deleting a key
     */
    @Test
    public void testDelete() {

        try {
            writer.write("SET key 0 10\r\n");
            writer.write("1234567890");
            writer.flush();
            String res = reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("1234567890", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

            writer.write("DELETE key\r\n");
            writer.flush();
            reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

            writer.write("GET key\r\n");
            writer.flush();
            res = reader.readLine();
            Assert.assertEquals("LEN 15", res);
            res = reader.readLine();
            Assert.assertEquals("ERROR not_found", res);

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
    public void testGetNotFound() {

        try {
            writer.write("GET notexists\r\n");
            writer.flush();
            String res = reader.readLine();
            Assert.assertEquals("LEN 15", res);
            res = reader.readLine();
            Assert.assertEquals("ERROR not_found", res);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Test setting an integer with no expiration
     */
    @Test
    public void testSetIntNoExpire() {

        try {
            writer.write("SET key 0 10\r\n");
            writer.write("1234567890");
            writer.flush();
            String res = reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("1234567890", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Test a malformed request
     */
    @Test
    public void testSetMalformed() {

        try {
            writer.write("SET key 0 0 10\r\n");
            writer.write("1234567890");
            writer.flush();
            String res = reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("ERROR malformed_request", res);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Test setting an integer with expiration
     */
    @Test
    public void testSetIntExpire() {

        try {
            writer.write("SET key 1 10\r\n");
            writer.write("1234567890");
            writer.flush();
            String res = reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("1234567890", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

            Thread.sleep(2000);

            writer.write("GET key\r\n");
            writer.flush();
            res = reader.readLine();
            Assert.assertEquals("LEN 15", res);
            res = reader.readLine();
            Assert.assertEquals("ERROR not_found", res);

        }
        catch (IOException | InterruptedException e )
        {
            e.printStackTrace();
        }

    }

    /**
     * Test touching a key
     */
    @Test
    public void testTouchFound() {

        try {
            writer.write("SET key 0 10\r\n");
            writer.write("1234567890");
            writer.flush();
            String res = reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("1234567890", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

            writer.write("TOUCH key 1\r\n");
            writer.flush();
            reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

            Thread.sleep(2000);

            writer.write("GET key\r\n");
            writer.flush();
            res = reader.readLine();
            Assert.assertEquals("LEN 15", res);
            res = reader.readLine();
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
            writer.write("SET key 1 10\r\n");
            writer.write("1234567890");
            writer.flush();
            String res = reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("1234567890", res);
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

            writer.write("TOUCH key 0\r\n");
            writer.flush();
            reader.readLine();
            res = reader.readLine();
            Assert.assertEquals("DONE", res);

            Thread.sleep(2000);

            writer.write("GET key\r\n");
            writer.flush();
            res = reader.readLine();
            Assert.assertEquals("LEN 10", res);
            res = reader.readLine();
            Assert.assertEquals("1234567890", res);
            res = reader.readLine();
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
            writer.write("TOUCH notexists 1\r\n");
            writer.flush();
            reader.readLine();
            String res = reader.readLine();
            Assert.assertEquals("DONE", res);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

}
