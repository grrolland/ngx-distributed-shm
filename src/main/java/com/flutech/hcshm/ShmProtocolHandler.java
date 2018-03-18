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
     * Expected Data Frame Size
     */
    private int expectedDataSize   = 0;

    /**
     * Current Key
     */
    private String key;
    /**
     * Current expiration value
     */
    private int expire;
    /**
     * Current incr value
     */
    private int incrValue;
    /**
     * Current incr init
     */
    private int incrInit;

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
                t.printStackTrace();
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
        switch(expectedMode) {
            case COMMAND:
                // get, set, touch, incr
                final String[] commandTokens = buffer.toString(PROTOCOL_ENCODING).split(COMMAND_LINE_DELIMITER);
                try{
                    final Command cmd = getCommand(commandTokens);
                    switch(cmd) {
                        case SET:
                            // SET KEY EXPIRE SIZE
                            assertTokens(commandTokens, 4);
                            key = getKey(commandTokens[1]);
                            expire = getExpire(commandTokens[2]);
                            expectedMode = FrameMode.DATA;
                            expectedDataSize = getSize(commandTokens[3]);
                            parser.fixedSizeMode(expectedDataSize);
                            break;
                        case GET:
                            // GET KEY
                            assertTokens(commandTokens, 2);
                            key = getKey(commandTokens[1]);
                            expectedMode = FrameMode.COMMAND;
                            socket.write(service.get(key), PROTOCOL_ENCODING);
                            socket.write(DONE, PROTOCOL_ENCODING);
                            parser.delimitedMode(PROTOCOL_DELIMITER);
                            break;
                        case TOUCH:
                            // TOUCH KEY EXPIRE
                            assertTokens(commandTokens, 3);
                            key = getKey(commandTokens[1]);
                            expire = getExpire(commandTokens[2]);
                            expectedMode = FrameMode.COMMAND;
                            service.touch(key, expire);
                            socket.write(DONE, PROTOCOL_ENCODING);
                            parser.delimitedMode(PROTOCOL_DELIMITER);
                            break;
                        case INCR:
                            // INCR KEY INCRVALUE INITVALUE
                            assertTokens(commandTokens, 4);
                            key = getKey(commandTokens[1]);
                            incrValue = getIncrValue(commandTokens[2]);
                            incrInit = getIncrValue(commandTokens[3]);
                            expectedMode = FrameMode.COMMAND;
                            socket.write(service.incr(key, incrValue, incrInit), PROTOCOL_ENCODING);
                            socket.write(DONE, PROTOCOL_ENCODING);
                            parser.delimitedMode(PROTOCOL_DELIMITER);
                            break;
                    }
                }
                catch (ProtocolException e)
                {
                    expectedMode = FrameMode.COMMAND;
                    parser.delimitedMode(PROTOCOL_DELIMITER);
                    socket.write(ERROR_MALFORMED_REQUEST, PROTOCOL_ENCODING);
                    break;
                }

                break;
            case DATA:

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
                break;
        }
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
    private String getKey(String commandToken) throws ProtocolException {
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
