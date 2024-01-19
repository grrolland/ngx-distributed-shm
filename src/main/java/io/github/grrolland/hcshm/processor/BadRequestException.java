package io.github.grrolland.hcshm.processor;

/**
 * The request is well-formed, but the command is not possible with the key stored in Hazelcast is not compatible with the processor
 */
public class BadRequestException extends RuntimeException {
    /**
     * Constructor
     *
     * @param cause
     *         the cause
     */
    public BadRequestException(final ClassCastException cause) {
        super(cause);
    }
}
