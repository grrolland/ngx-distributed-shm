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

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedFunction0;
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
import java.time.Duration;

/**
 * Global Protocol Test Case
 */
public abstract class AbstractHCSHMGetTestCase {

    // Wait duration in second
    public static final int OPEN_SOCKET_WAIT_DURATION = 2;

    // Max retry
    public static final int SOCKET_RETRY_LOOP = 10;

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

    protected BufferedReader getReader() {
        return reader;
    }

    protected BufferedWriter getWriter() {
        return writer;
    }

    /**
     * get Retry
     *
     * @return the retry
     */
    private static Retry getRetry() {
        RetryConfig config = RetryConfig.custom()
                // Max
                .maxAttempts(SOCKET_RETRY_LOOP)
                // Sleep
                .waitDuration(Duration.ofSeconds(OPEN_SOCKET_WAIT_DURATION))
                // Retry on IOException
                .retryExceptions(IOException.class)
                // Fail
                .failAfterMaxAttempts(true)
                //
                .writableStackTraceEnabled(true)
                // Build
                .build();

        // Create the RetryRegistry
        RetryRegistry registry = RetryRegistry.of(config);

        // Create the retry
        return registry.retry("openSocket", config);
    }

    /**
     * Wait (2000 ms)
     *
     * @throws InterruptedException
     *         exception when  trying to sleep the current thread
     */
    public static void pause() throws InterruptedException {
        Thread.sleep(2000); // NOSONAR
    }

    /**
     * Init Socket, Reader and Writer
     */
    @Before
    public void before() throws Throwable {
        sock = this.openSocketWithRetry();
        reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
        flushAll();
    }

    /**
     * Flush default region
     *
     * @throws IOException
     *         I/O exception
     */
    public void flushAll() throws IOException {
        this.flushAll("");
    }

    /**
     * Flush ALL region
     *
     * @param region
     *         the region
     * @throws IOException
     *         I/O exception
     */
    public void flushAll(String region) throws IOException {
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
            if (sock != null && !sock.isClosed()) {
                sock.shutdownInput();
                sock.shutdownOutput();
                sock.close();
            }
        } catch (IOException e) {
            // Ignore shutdown issue
            e.printStackTrace();
        }
    }

    /**
     * Assert that response get the value <code>expect</code>
     *
     * @param expect
     *         expected value
     * @throws IOException
     *         I/O Exception
     */
    public void assertResponseGetValue(String expect) throws IOException {
        String res = getReader().readLine();
        Assert.assertEquals("LEN " + expect.length(), res);
        res = getReader().readLine();
        Assert.assertEquals(expect, res);
        res = getReader().readLine();
        Assert.assertEquals("DONE", res);
    }

    /**
     * Assert response is key not found
     *
     * @throws IOException
     *         I/O Exception
     */
    public void assertResponseNotFound() throws IOException {
        String res = getReader().readLine();
        Assert.assertEquals("ERROR not_found", res);
    }

    /**
     * Assert response is malformed request
     *
     * @throws IOException
     *         I/O Exception
     */
    public void assertResponseMalFormedRequest() throws IOException {
        String res = getReader().readLine();
        Assert.assertEquals("ERROR malformed_request", res);
    }

    /**
     * Assert response is DONE
     *
     * @throws IOException
     */
    public void assertResponseDone() throws IOException {
        String res = getReader().readLine();
        Assert.assertEquals("DONE", res);
    }

    /**
     * Open socket with retry
     *
     * @return Socket
     * @throws InterruptedException
     *         exception while opening socket
     */
    private Socket openSocketWithRetry() throws Throwable {
        Retry retry = getRetry();
        CheckedFunction0<Socket> retryingOpenSocket = Retry.decorateCheckedSupplier(retry, this::openSocket);
        return retryingOpenSocket.apply();
    }

    private Socket openSocket() throws IOException {
        return new Socket(InetAddress.getByName("localhost"), 40321);
    }
}
