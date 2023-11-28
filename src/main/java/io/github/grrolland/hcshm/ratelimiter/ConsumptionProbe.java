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
package io.github.grrolland.hcshm.ratelimiter;

/**
 * Describes token consumed, and number of tokens remaining
 * <p>
 * remainingTokens -1 means no tokens have been consumed
 * remainingTokens 0 means there is no more tokens to consume
 */
public class ConsumptionProbe {

    /**
     * Number of remaining tokens
     */
    private final int remainingTokens;

    /**
     * Return the number of remaining available tokens or -1 if capacity was exceeded
     *
     * @return the number of remaining available tokens or -1
     */
    public int getRemainingTokens() {
        return this.remainingTokens;

    }

    /**
     * Constructor
     *
     * @param remainingTokens
     *         the number of remaining token
     */
    ConsumptionProbe(int remainingTokens) {
        this.remainingTokens = remainingTokens;
    }
}
