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


import java.io.Serializable;

/**
 * Value in the SHM Map
 */
public class ShmValue implements Serializable {

    /**
     * The value
     */
    private Object value = null;
    /**
     * The expiration deadline
     */
    private long deadline = 0;
    /**
     * Indicate if the value expire
     */
    private boolean doExpire = false;

    /**
     * Constructor
     *
     * @param newval the new value
     * @param expire the expiration in second
     */
    public ShmValue(Object newval, int expire) {
        value = newval;
        if (expire != 0) {
            deadline = System.currentTimeMillis() + expire * 1000;
            doExpire = true;
        }
    }

    /**
     * Get the value
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Expire the value
     * @param sectime expiration in second
     */
    public void expire(int sectime) {
        if (sectime != 0) {
            deadline = System.currentTimeMillis() + sectime * 1000;
            doExpire = true;
        }
        else
        {
            doExpire = false;
        }
    }

    /**
     * Get le lasting time for the value
     *
     * When the time to the expiration deadline is lower than 1, return -1.
     *
     * tha mean the value should expire immediately
     *
     * @return the time to the expiration deadline
     */
    public int getLastingTime() {
        if (doExpire) {
            final double lt = (deadline - System.currentTimeMillis()) / 1000d;
            if (lt < 1.0) {
                return -1;
            }
            else {
                return (int) lt;
            }
        }
        else
        {
            return 0;
        }
    }


}
