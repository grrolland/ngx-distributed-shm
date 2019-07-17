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
 * The FLUSHALL Command
 */
public class FlushAllCommand extends Command {

    /**
     * Default Constructor
     * @param service the shm service
     */
    FlushAllCommand(ShmService service) {
        super(service);
    }

    /**
     * Execute the command
     * @param commandTokens the protocol tokens argument of the command
     * @return the result of the command 'protocol encoded'
     * @throws ProtocolException protocol exception
     */
    public String execute(String[] commandTokens) {
        final StringBuilder response = new StringBuilder();
        try
        {
            assertTokens(commandTokens, 1, 2);
            String region = getKey(commandTokens.length == 1 ? null : commandTokens[1]);
            getService().flushall(region);
            writeDone(response);
        }
        catch (ProtocolException e)
        {
            writeMalformedRequest(response);
        }
        return response.toString();
    }

}
