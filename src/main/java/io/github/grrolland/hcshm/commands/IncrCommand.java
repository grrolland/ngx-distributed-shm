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
package io.github.grrolland.hcshm.commands;

import io.github.grrolland.hcshm.ProtocolException;
import io.github.grrolland.hcshm.ShmService;

/**
 * The INCR Command
 */
public class IncrCommand extends Command {

    /**
     * Default Constructor
     * @param service the shm service
     */
    IncrCommand(ShmService service) {
        super(service);
    }

    /**
     * Execute the command
     * @param commandTokens the protocol tokens argument of the command
     * @return the result of the command 'protocol encoded'
     */
    public String execute(String[] commandTokens) {
        final StringBuilder response = new StringBuilder();
        try
        {
            assertTokens(commandTokens, 4);
            String key = getKey(commandTokens[1]);
            int incr = getIncrValue(commandTokens[2]);
            int initial = getIncrValue(commandTokens[3]);
            String value = getService().incr(key, incr, initial);
            writeLen(response, value);
            writeValue(response, value);
            writeDone(response);
        }
        catch (ProtocolException e)
        {
            writeMalformedRequest(response);
        }
        return response.toString();
    }
}
