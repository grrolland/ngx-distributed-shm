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
package com.flutech.hcshm.commands;

import com.flutech.hcshm.ProtocolException;
import com.flutech.hcshm.ShmService;

/**
 * Abstract Protocol command controller
 * Execute command and build protocol response
 */
public abstract class Command {

    /**
     * Protocol Encoding
     */
    protected static final String PROTOCOL_ENCODING = "UTF-8";
    /**
     * Protocol response : DONE
     */
    protected static final String DONE = "DONE";
    /**
     * Protocol response : DONE
     */
    protected static final String LEN = "LEN ";
    /**
     * Protocol response : DONE
     */
    protected static final String RESPONSE_LINE_DELIMITER = "\r\n";
    /**
     * Protocol response : Error malformed resuqest
     */
    protected static final String ERROR_MALFORMED_REQUEST = "ERROR malformed_request";
    /**
     * Protocol response : Error malformed request
     */
    protected static final String ERROR_UNEXPECTED_DATA = "ERROR unexpected_data";
    /**
     * Protocol response : Error Not Found
     */
    protected static final String ERROR_NOT_FOUND = "ERROR not_found";

    /**
     * SHM Service
     */
    private ShmService service = null;

    /**
     * Package private constructor for factory only
     * @param service th SHM Service
     */
    Command(ShmService service) {
        this.service = service;
    }

    /**
     * Get the SHM service
     * @return the service
     */
    protected ShmService getService() {
        return service;
    }

    /**
     * Execute the command
     * @param comandTokens the protocol tokens argument of the command
     * @return the result of the command 'protocol encoded'
     * @throws ProtocolException protocol exception
     */
    public abstract String execute(String[] comandTokens);

    /**
     * Execute the command
     * @param buffer the data buffer
     * @return the result of the command 'protocol encoded'
     * @throws ProtocolException protocol exception
     */
    public String executeDataPart(String buffer) {
        final StringBuilder response = new StringBuilder();
        response.append(ERROR_UNEXPECTED_DATA);
        response.append(RESPONSE_LINE_DELIMITER);
        return response.toString();
    }

    /**
     * Get if this command is a termination command and the socket must be closed after the execution
     * @return true if this is a termination command
     */
    public boolean isTerminationCommand() {
        return false;
    }

    /**
     * Get if this command need a data part
     * @return true if this command need a data part
     */
    public boolean needsDataPart() {
        return false;
    }

    /**
     * Get the size of the data part if needed
     * @return the size of the data part if needed
     */
    public int getDataPartSize() {
        return 0; // NOSONAR
    }

    /**
     * Assert command tokens contains expected token number
     * @param commandTokens the command tokens
     * @param expectedTokens the expected token numbers
     * @throws ProtocolException if the command token numbers is different from expected
     */
    protected void assertTokens(String[] commandTokens, int expectedTokens) throws ProtocolException {
        if(commandTokens.length != expectedTokens) {
            throw  new ProtocolException();
        }
    }

    /**
     * Assert command tokens contains expected token number
     * @param commandTokens the command tokens
     * @param minToken the min expected token numbers
     * @param maxToken the max expected token numbers
     * @throws ProtocolException if the command token numbers is different from expected
     */
    protected void assertTokens(String[] commandTokens, int minToken, int maxToken) throws ProtocolException {
        if(commandTokens.length < minToken || commandTokens.length > maxToken) {
            throw  new ProtocolException();
        }
    }

    /**
     * Get the key from command token
     * @param commandToken the command token
     * @return the key
     * @throws ProtocolException if unable to get the key from command token
     */
    protected String getKey(String commandToken) {
        return commandToken;
    }

    /**
     * Get the expire value form command token
     * @param commandToken the command token
     * @return the expire value
     * @throws ProtocolException if unable to get the expire value from command token
     */
    protected int getExpire(String commandToken) throws ProtocolException {
        try
        {
            final int l_expire =  Integer.parseInt(commandToken);
            if (l_expire < 0) {
                throw new ProtocolException();
            }
            return l_expire;
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
    protected int getIncrValue(String commandToken) throws ProtocolException {
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
    protected int getSize(String commandToken) throws ProtocolException {
        try
        {
            final int size = Integer.parseInt(commandToken);
            if (size <= 0) {
                throw new ProtocolException();
            }
            return size;
        }
        catch(NumberFormatException e)
        {
            throw new ProtocolException();
        }
    }

    /**
     * Write the LEN protocol line
     * @param response the constructing response
     * @param value the value which the length to write.
     */
    protected void writeLen(StringBuilder response, String value) {
        response.append(LEN);
        response.append(Integer.toString(value.length()));
        response.append(RESPONSE_LINE_DELIMITER);
    }
    /**
     * Write the LEN protocol line
     * @param response the constructing response
     * @param value the value
     */
    protected void writeValue(StringBuilder response, String value) {
        response.append(value);
        response.append(RESPONSE_LINE_DELIMITER);
    }
    /**
     * Write the LEN protocol line
     * @param response the constructing response
     */
    protected void writeDone(StringBuilder response) {
        response.append(DONE);
        response.append(RESPONSE_LINE_DELIMITER);
    }
    /**
     * Write the LEN protocol line
     * @param response the constructing response
     */
    protected void writeMalformedRequest(StringBuilder response) {
        response.append(ERROR_MALFORMED_REQUEST);
        response.append(RESPONSE_LINE_DELIMITER);
    }
    /**
     * Write the LEN protocol line
     * @param response the constructing response
     */
    protected void writeNotFound(StringBuilder response) {
        response.append(ERROR_NOT_FOUND);
        response.append(RESPONSE_LINE_DELIMITER);
    }
}
