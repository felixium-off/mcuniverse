package org.mcuniverse.api.instance.exception;

/**
 * Base checked exception for all instance-related operations.
 * Inspired by AdvancedSlimePaper's exception handling architecture.
 */
public class InstanceException extends Exception {
    
    public InstanceException(String message) {
        super(message);
    }
    
    public InstanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
