package org.mcuniverse.api.world.exception;

/**
 * Exception thrown when compression or decompression of world data fails.
 */
public class CompressionException extends WorldStorageException {
    
    public CompressionException(String message) {
        super(message);
    }
    
    public CompressionException(String message, Throwable cause) {
        super(message, cause);
    }
}
