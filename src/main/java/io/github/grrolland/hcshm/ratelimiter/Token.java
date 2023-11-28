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

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

/**
 * A token consumed at a specific date
 */
public class Token implements Serializable {

    /**
     * Expiration
     */
    private final long createdAt;

    /**
     * Constructor
     */
    public Token() {
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * @param duration
     *         Duration
     * @return true if expired
     */
    boolean isExpired(Duration duration) {
        // check if expiration date is before now
        return Instant.ofEpochMilli(this.createdAt).plus(duration).isBefore(Instant.now());
    }
}
