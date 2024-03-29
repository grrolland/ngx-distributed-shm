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

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Handler singleton for the hazelcast backend instance
 */
public class HazelcastInstanceHandler {
    /**
     * Hazelcast instance
     */
    private static HazelcastInstance instance = null;

    /**
     * Hazelcast instance getter
     *
     * @return the unique hazelcast instance
     */
    public static synchronized HazelcastInstance getInstance() {
        if (instance == null) {
            instance = Hazelcast.newHazelcastInstance();
        }
        return instance;
    }

    /**
     * Private Constructor (utility class)
     */
    private HazelcastInstanceHandler() {

    }

}
