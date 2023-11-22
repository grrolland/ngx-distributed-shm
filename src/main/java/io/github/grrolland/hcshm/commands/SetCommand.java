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
package io.github.grrolland.hcshm.commands;

import io.github.grrolland.hcshm.ProtocolException;
import io.github.grrolland.hcshm.ShmService;

/**
 * The Set Command
 */
public class SetCommand extends Command {

    /**
     * The key
     */
    private String key = null;

    /**
     * The expiration in milliseconds
     */
    private long expire = 0;

    /**
     * The length
     */
    private int length = 0;

    /**
     * Need a data part ?
     */
    private boolean needsDataPart = false;

    /**
     * Get the size of the data part if needed
     *
     * @return the size of the data part if needed
     */
    @Override
    public int getDataPartSize() {
        return length;
    }

    /**
     * Execute the command
     *
     * @param commandTokens
     *         the protocol tokens argument of the command
     * @return the result of the command 'protocol encoded'
     */
    public String execute(String[] commandTokens) {
        final StringBuilder response = new StringBuilder();
        try {
            assertTokens(commandTokens, 4);
            key = getKey(commandTokens[1]);
            expire = getExpire(commandTokens[2]);
            length = getSize(commandTokens[3]);
            needsDataPart = true;
        } catch (ProtocolException e) {
            writeMalformedRequest(response);
        }
        return response.toString();
    }

    /**
     * Execute the command
     *
     * @param buffer
     *         the data buffer
     * @return the result of the command 'protocol encoded'
     */
    @Override
    public String executeDataPart(String buffer) {
        final StringBuilder response = new StringBuilder();
        String value;
        try {
            value = getService().set(key, Long.parseLong(buffer), expire);
        } catch (NumberFormatException e) {
            value = getService().set(key, buffer, expire);
        }
        writeLen(response, value);
        writeValue(response, value);
        writeDone(response);
        needsDataPart = false;
        length = 0;
        return response.toString();
    }

    /**
     * Get if this command need a data part
     *
     * @return true if this command need a data part
     */
    @Override
    public boolean needsDataPart() {
        return needsDataPart;
    }

    /**
     * Default Constructor
     *
     * @param service
     *         the shm service
     */
    SetCommand(ShmService service) {
        super(service);
    }

}
