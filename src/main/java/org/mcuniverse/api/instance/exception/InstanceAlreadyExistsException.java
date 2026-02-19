package org.mcuniverse.api.instance.exception;

/**
 * Thrown when attempting to create an instance with a name that already exists.
 */
public class InstanceAlreadyExistsException extends InstanceException {
    
    public InstanceAlreadyExistsException(String instanceName) {
        super("Instance already exists: " + instanceName);
    }
}
