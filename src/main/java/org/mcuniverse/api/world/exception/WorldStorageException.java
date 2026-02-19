package org.mcuniverse.api.world.exception;

/**
 * Base exception for all world storage operations.
 * Thrown when operations involving world data storage/retrieval fail.
 */
public class WorldStorageException extends Exception {
    
    public WorldStorageException(String message) {
        super(message);
    }
    
    public WorldStorageException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public WorldStorageException(Throwable cause) {
        super(cause);
    }
}
