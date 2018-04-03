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

import com.flutech.hcshm.commands.Command;
import com.flutech.hcshm.commands.CommandFactory;
import com.flutech.hcshm.commands.CommandVerb;
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
     * Command Factory
     */
    private CommandFactory commandFactory = null;
    /**
     * Vertx record Parser
     */
    private RecordParser parser = null;
    /**
     * Expected Protocol Decoding Mode
     */
    private FrameMode expectedMode = FrameMode.COMMAND;
    /**
     * Current Command
     */
    private Command currentCommand;

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
            .exceptionHandler(t -> socket.close())
            .handler(this);
        this.service = service;
        this.commandFactory = new CommandFactory();
        this.commandFactory.setService(service);
    }

    /**
     * Protocol Handler implementation
     * @param buffer vertx buffer
     */
    @Override
    public void handle(Buffer buffer) {

        if (expectedMode == FrameMode.COMMAND) {

            final String[] commandTokens = buffer.toString(PROTOCOL_ENCODING).split(COMMAND_LINE_DELIMITER);
            currentCommand = commandFactory.get(commandTokens);
            final String result = currentCommand.execute(commandTokens);

            socket.write(result, PROTOCOL_ENCODING);

            if (currentCommand.isTerminationCommand()) {
                socket.close();
            }
            else if (currentCommand.needsDataPart())
            {
                expectedMode = FrameMode.DATA;
                parser.fixedSizeMode(currentCommand.getDataPartSize());
            }
            else
            {
                expectedMode = FrameMode.COMMAND;
                parser.delimitedMode(PROTOCOL_DELIMITER);
            }

        }
        else {
            final String result = currentCommand.executeDataPart(buffer.toString(PROTOCOL_ENCODING));
            socket.write(result, PROTOCOL_ENCODING);
            expectedMode = FrameMode.COMMAND;
            parser.delimitedMode(PROTOCOL_DELIMITER);
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
}
