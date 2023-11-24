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
package io.github.grrolland.hcshm;

import java.io.Serializable;

/**
 * Value in the SHM Map
 */
public class ShmValue implements Serializable {

    /**
     * The value
     */
    private final String value;

    /**
     * The expiration deadline
     */
    private long deadline = 0;

    /**
     * Indicate if the value expire
     */
    private boolean doExpire = false;

    /**
     * Get the value
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get le lasting time for the value
     * <p>
     * When the time to the expiration deadline is lower than 100 milliseconds, return -1.
     * That mean the value should expire immediately
     *
     * @return the time to the expiration deadline
     */
    public long getLastingTime() {
        if (doExpire) {
            final long lt = deadline - System.currentTimeMillis();
            if (lt < 100) {
                return -1;
            } else {
                return lt;
            }
        } else {
            return 0;
        }
    }

    /**
     * Constructor
     *
     * @param newval
     *         the new value
     * @param expire
     *         the expiration in milliseconds
     */
    public ShmValue(String newval, long expire) {
        value = newval;
        this.expire(expire);
    }

    /**
     * Expire the value
     *
     * @param sectime
     *         expiration in milliseconds
     */
    public void expire(long sectime) {
        if (sectime != 0) {
            deadline = System.currentTimeMillis() + sectime;
            doExpire = true;
        } else {
            doExpire = false;
        }
    }

}
