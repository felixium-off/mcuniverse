package org.mcuniverse.api.instance.exception;

/**
 * Thrown when instance creation or loading fails due to internal errors.
 */
public class InstanceLoadException extends InstanceException {
    
    public InstanceLoadException(String message) {
        super(message);
    }
    
    public InstanceLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
