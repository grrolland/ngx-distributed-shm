package io.github.grrolland.hcshm;

import java.io.Serializable;

/***
 * Base class for value stored in Hazelcast
 */
public abstract class AbstractShmValue implements Serializable {

    /**
     * Get string value
     *
     * @return the value as string
     */
    public abstract String getValue();
}
