package org.mcuniverse.api.instance.exception;

/**
 * Thrown when trying to retrieve or operate on a non-existent instance.
 */
public class InstanceNotFoundException extends InstanceException {
    
    public InstanceNotFoundException(String instanceName) {
        super("Instance not found: " + instanceName);
    }
}
