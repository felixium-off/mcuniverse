package org.mcuniverse.api.world;

import org.mcuniverse.api.world.exception.WorldStorageException;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for storing and retrieving world data.
 * Implementations handle different storage backends (filesystem, MongoDB, etc.)
 */
public interface WorldStorage {
    
    /**
     * Save world data to storage.
     * 
     * @param worldId Unique identifier for the world
     * @param polarData Polar format world data
     * @throws WorldStorageException if save operation fails
     */
    void saveWorld(@NotNull String worldId, byte[] polarData) throws WorldStorageException;
    
    /**
     * Load world data from storage.
     * 
     * @param worldId Unique identifier for the world
     * @return Polar format world data
     * @throws WorldStorageException if load operation fails or world not found
     */
    byte[] loadWorld(@NotNull String worldId) throws WorldStorageException;
    
    /**
     * Check if a world exists in storage.
     * 
     * @param worldId Unique identifier for the world
     * @return true if the world exists, false otherwise
     */
    boolean exists(@NotNull String worldId);
    
    /**
     * Delete a world from storage.
     * 
     * @param worldId Unique identifier for the world
     * @throws WorldStorageException if delete operation fails
     */
    void deleteWorld(@NotNull String worldId) throws WorldStorageException;
}
