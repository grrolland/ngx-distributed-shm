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

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;

/**
 * Vertx Protocol Handler
 *
 * @author grrolland
 */
public class ShmProtocolHandler implements Handler<Buffer> {

    /**
     * Protocol Encoding
     */
    public static final String PROTOCOL_ENCODING = "UTF-8";
    /**
     * Protocol response : DONE
     */
    public static final String DONE = "\r\nDONE\r\n";
    /**
     * Protocol response : Error malformed resuqest
     */
    public static final String ERROR_MALFORMED_REQUEST = "\r\nERROR malformed_request\r\n";
    /**
     * Protocol Command Delimiter
     */
    public static final String PROTOCOL_DELIMITER = "\r\n";
    /**
     * Protocol Command Line Delimiter
     */
    public static final String COMMAND_LINE_DELIMITER = " ";

    /**
     * Vertx NetSocket
     */
    private NetSocket socket = null;
    /**
     * Shared Memory Service
     */
    private ShmService service = null;
    /**
     * Vertx record Parser
     */
    private RecordParser parser = null;
    /**
     * Expected Protocol Decoding Mode
     */
    private FrameMode expectedMode = FrameMode.COMMAND;
    /**
     * Current Key
     */
    private String key;
    /**
     * Current expiration value
     */
    private int expire;

    /**
     * Protocol Handler Instance Factory Method
     * @param socket vertx socket
     * @param service shm service
     */
    public static void create(NetSocket socket, ShmService service) {
        new ShmProtocolHandler(socket,service);
    }

    /**
     * Public Constructor
     * @param socket vertx socket
     * @param service shm service
     */
    public ShmProtocolHandler(NetSocket socket, ShmService service) {
        this.socket = socket;
        this.parser = RecordParser.newDelimited(PROTOCOL_DELIMITER, socket);
        this.parser.endHandler(v -> socket.close())
            .exceptionHandler(t -> {
                socket.close();
            })
            .handler(this);
        this.service = service;
    }

    /**
     * Protocol Handler implementation
     * @param buffer vertx buffer
     */
    @Override
    public void handle(Buffer buffer) {
        try {
            if (expectedMode == FrameMode.COMMAND) {
                final String[] commandTokens = buffer.toString(PROTOCOL_ENCODING).split(COMMAND_LINE_DELIMITER);
                final Command cmd = getCommand(commandTokens);
                switch (cmd) {
                    case SET:
                        doSetCommandPart(commandTokens);
                        break;
                    case GET:
                        doGet(commandTokens);
                        break;
                    case TOUCH:
                        doTouch(commandTokens);
                        break;
                    case INCR:
                        doIncr(commandTokens);
                        break;
                }
            }
            else {
                doSetDataPart(buffer);
            }
        } catch (ProtocolException e) {
            expectedMode = FrameMode.COMMAND;
            parser.delimitedMode(PROTOCOL_DELIMITER);
            socket.write(ERROR_MALFORMED_REQUEST, PROTOCOL_ENCODING);
        }
    }

    /**
     * Incrementation command :
     *
     * INCR KEY INCRVALUE INITVALUE
     *
     * @param commandTokens the command tokens
     * @throws ProtocolException if malformed command
     */
    private void doIncr(String[] commandTokens) throws ProtocolException {
        // INCR KEY INCRVALUE INITVALUE
        assertTokens(commandTokens, 4);
        key = getKey(commandTokens[1]);
        expectedMode = FrameMode.COMMAND;
        socket.write(service.incr(key, getIncrValue(commandTokens[2]), getIncrValue(commandTokens[3])), PROTOCOL_ENCODING);
        socket.write(DONE, PROTOCOL_ENCODING);
        parser.delimitedMode(PROTOCOL_DELIMITER);
    }

    /**
     * Touch command :
     *
     * TOUCH KEY EXPIRE
     *
     * @param commandTokens the command tokens
     * @throws ProtocolException if malformed command
     */
    private void doTouch(String[] commandTokens) throws ProtocolException {
        assertTokens(commandTokens, 3);
        key = getKey(commandTokens[1]);
        expire = getExpire(commandTokens[2]);
        expectedMode = FrameMode.COMMAND;
        service.touch(key, expire);
        socket.write(DONE, PROTOCOL_ENCODING);
        parser.delimitedMode(PROTOCOL_DELIMITER);
    }

    /**
     * Get command :
     *
     * GET KEY
     *
     * @param commandTokens the command tokens
     * @throws ProtocolException if malformed command
     */
    private void doGet(String[] commandTokens) throws ProtocolException {
        assertTokens(commandTokens, 2);
        key = getKey(commandTokens[1]);
        expectedMode = FrameMode.COMMAND;
        socket.write(service.get(key), PROTOCOL_ENCODING);
        socket.write(DONE, PROTOCOL_ENCODING);
        parser.delimitedMode(PROTOCOL_DELIMITER);
    }

    /**
     * Set command : command part
     *
     * SET KEY EXPIRE SIZE
     * DATA
     *
     * @param commandTokens the command tokens
     * @throws ProtocolException if malformed command
     */
    private void doSetCommandPart(String[] commandTokens) throws ProtocolException {
        assertTokens(commandTokens, 4);
        key = getKey(commandTokens[1]);
        expire = getExpire(commandTokens[2]);
        expectedMode = FrameMode.DATA;
        parser.fixedSizeMode(getSize(commandTokens[3]));
    }

    /**
     * Set command : data part
     *
     * SET KEY EXPIRE SIZE
     * DATA
     *
     * @param buffer the data buffer
     */
    private void doSetDataPart(Buffer buffer) {
        String data = buffer.toString(PROTOCOL_ENCODING);
        String value;
        try
        {
            value = service.set(key, Long.valueOf(data), expire);
        }
        catch (NumberFormatException e)
        {
            value = service.set(key, data, expire);
        }
        socket.write(PROTOCOL_DELIMITER, PROTOCOL_ENCODING);
        socket.write(value, PROTOCOL_ENCODING);
        socket.write(DONE, PROTOCOL_ENCODING);
        expectedMode = FrameMode.COMMAND;
        parser.delimitedMode(PROTOCOL_DELIMITER);
    }


    /**
     * Get Command from command token
     * @param commandTokens the command tokens
     * @return the command
     * @throws ProtocolException if command is unknown
     */
    private Command getCommand(String[] commandTokens) throws ProtocolException {
        try {
            return Command.valueOf(commandTokens[0].toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            throw new ProtocolException();
        }
    }

    /**
     * Assert command tokens contains expected token number
     * @param commandTokens the command tokens
     * @param expectedTokens the expected token numbers
     * @throws ProtocolException if the command token numbers is different from expected
     */
    private void assertTokens(String[] commandTokens, int expectedTokens) throws ProtocolException {
        if(commandTokens.length != expectedTokens) {
            throw  new ProtocolException();
        }
    }

    /**
     * Get the key form command token
     * @param commandToken the command token
     * @return the key
     * @throws ProtocolException if unable to get the key from command token
     */
    private String getKey(String commandToken) {
        return commandToken;
    }

    /**
     * Get the expire value form command token
     * @param commandToken the command token
     * @return the expire value
     * @throws ProtocolException if unable to get the expire value from command token
     */
    private int getExpire(String commandToken) throws ProtocolException {
        try
        {
            return Integer.valueOf(commandToken);
        }
        catch(NumberFormatException e)
        {
            throw new ProtocolException();
        }
    }

    /**
     * get the size of data from command token
     * @param commandToken the command token
     * @return the size of the data frame
     * @throws ProtocolException if unable to get the size of the data frame from command token
     */
    private int getSize(String commandToken) throws ProtocolException {
        try
        {
            return Integer.valueOf(commandToken);
        }
        catch(NumberFormatException e)
        {
            throw new ProtocolException();
        }
    }

    /**
     * get the incr value from command token
     * @param commandToken the command token
     * @return the incr value
     * @throws ProtocolException if unable to get the incr value frame from command token
     */
    private int getIncrValue(String commandToken) throws ProtocolException {
        try
        {
            return Integer.valueOf(commandToken);
        }
        catch(NumberFormatException e)
        {
            throw new ProtocolException();
        }
    }

    /**
     * The frame mode
     */
    private enum FrameMode {
        /**
         * Command line Mode
         */
        COMMAND,
        /**
         * Data mode
         */
        DATA
    }

    /**
     * The known Command
     */
    private enum Command {
        /**
         * The GET command
         */
        GET,
        /**
         * The SET command
         */
        SET,
        /**
         * The TOUCH command
         */
        TOUCH,
        /**
         * The INCR command
         */
        INCR
    }

    /**
     * The Protocol Exception
     */
    private class ProtocolException extends Exception {}
}
