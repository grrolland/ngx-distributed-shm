package com.flutech.hcshm;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;

public class ShmProtocolHandler implements Handler<Buffer> {

    private NetSocket socket = null;

    private ShmService service = null;

    private RecordParser parser = null;

    private FrameMode expectedMode = FrameMode.COMMAND;
    private int expectedDataSize   = 0;

    private String key;
    private int expire;
    private int incrValue;
    private int incrInit;

    public static void create(NetSocket socket, ShmService service) {
        new ShmProtocolHandler(socket,service);
    }

    public ShmProtocolHandler(NetSocket socket, ShmService service) {
        this.socket = socket;
        this.parser = RecordParser.newDelimited("\r\n", socket);
        this.parser.endHandler(v -> socket.close())
            .exceptionHandler(t -> {
                t.printStackTrace();
                socket.close();
            })
            .handler(this);
        this.service = service;
    }

    @Override
    public void handle(Buffer buffer) {
        switch(expectedMode) {
            case COMMAND:
                // get, set, touch, incr
                final String[] commandTokens = buffer.toString("UTF-8").split(" ");
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
                            socket.write(service.get(key), "UTF-8");
                            socket.write("\r\nDONE\r\n", "UTF-8");
                            parser.delimitedMode("\r\n");
                            break;
                        case TOUCH:
                            // TOUCH KEY EXPIRE
                            assertTokens(commandTokens, 3);
                            key = getKey(commandTokens[1]);
                            expire = getExpire(commandTokens[2]);
                            expectedMode = FrameMode.COMMAND;
                            service.touch(key, expire);
                            socket.write("\r\nDONE\r\n", "UTF-8");
                            parser.delimitedMode("\r\n");
                            break;
                        case INCR:
                            // INCR KEY INCRVALUE INITVALUE
                            assertTokens(commandTokens, 4);
                            key = getKey(commandTokens[1]);
                            incrValue = getIncrValue(commandTokens[2]);
                            incrInit = getIncrValue(commandTokens[3]);
                            expectedMode = FrameMode.COMMAND;
                            socket.write(service.incr(key, incrValue, incrInit), "UTF-8");
                            socket.write("\r\nDONE\r\n", "UTF-8");
                            parser.delimitedMode("\r\n");
                            break;
                    }
                }
                catch (ProtocolException e)
                {
                    expectedMode = FrameMode.COMMAND;
                    parser.delimitedMode("\r\n");
                    socket.write("\r\nERROR malformed_request\r\n", "UTF-8");
                    break;
                }

                break;
            case DATA:

                String data = buffer.toString("UTF-8");
                String value;
                try
                {
                    value = service.set(key, Long.valueOf(data), expire);
                }
                catch (NumberFormatException e)
                {
                    value = service.set(key, data, expire);
                }

                socket.write("\r\n", "UTF-8");
                socket.write(value, "UTF-8");
                socket.write("\r\nDONE\r\n", "UTF-8");
                expectedMode = FrameMode.COMMAND;
                parser.delimitedMode("\r\n");
                break;
        }
    }



    private Command getCommand(String[] commandTokens) throws ProtocolException {
        try {
            return Command.valueOf(commandTokens[0].toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            throw new ProtocolException();
        }
    }

    private void assertTokens(String[] commandTokens, int expectedTokens) throws ProtocolException {
        if(commandTokens.length != expectedTokens) {
            throw  new ProtocolException();
        }
    }

    private String getKey(String commandToken) throws ProtocolException {
        return commandToken;
    }

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

    private enum FrameMode {
        COMMAND,
        DATA
    }

    private enum Command {
        GET,
        SET,
        TOUCH,
        INCR
    }

    private class ProtocolException extends Exception {}
}
