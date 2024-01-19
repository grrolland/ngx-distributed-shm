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

import io.github.grrolland.hcshm.ShmService;

/**
 * Build the protocol command controller
 */
public class CommandFactory {

    /**
     * SHM Service
     */
    private ShmService service = null;

    /**
     * Set the SHM Service
     *
     * @param service
     *         the service
     */
    public void setService(ShmService service) {
        this.service = service;
    }

    /**
     * Get the command from the protocol verb
     *
     * @param commandTokens
     *         the command tokens
     * @return the corresponding command
     */
    public Command get(String[] commandTokens) {
        final Command command;
        switch (getCommand(commandTokens)) {
            case SET:
                command = new SetCommand(service);
                break;
            case GET:
                command = new GetCommand(service);
                break;
            case TOUCH:
                command = new TouchCommand(service);
                break;
            case QUIT:
                command = new QuitCommand(service);
                break;
            case DELETE:
                command = new DeleteCommand(service);
                break;
            case INCR:
                command = new IncrCommand(service);
                break;
            case FLUSHALL:
                command = new FlushAllCommand(service);
                break;
            case RATE_LIMITER:
                command = new RateLimiterCommand(service);
                break;
            default:
                command = new UnknownCommand(service);
                break;
        }
        return command;
    }

    /**
     * Get Command from command token
     *
     * @param commandTokens
     *         the command tokens
     * @return the command
     */
    private CommandVerb getCommand(String[] commandTokens) {
        try {
            return CommandVerb.valueOf(commandTokens[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            return CommandVerb.UNKNOWN;
        }
    }

}
