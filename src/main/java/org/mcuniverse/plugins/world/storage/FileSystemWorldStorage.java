package org.mcuniverse.plugins.world.storage;

import org.jetbrains.annotations.NotNull;
import org.mcuniverse.api.world.WorldStorage;
import org.mcuniverse.api.world.exception.WorldStorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Filesystem-based world storage implementation.
 * Used for storing static dungeon room templates.
 */
public class FileSystemWorldStorage implements WorldStorage {
    
    private final Path baseDirectory;
    
    /**
     * Create a filesystem storage with the default base directory (worlds_polar/rooms).
     */
    public FileSystemWorldStorage() {
        this(Paths.get("worlds_polar/rooms"));
    }
    
    /**
     * Create a filesystem storage with a custom base directory.
     * 
     * @param baseDirectory Base directory for storing .polar files
     */
    public FileSystemWorldStorage(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
        try {
            Files.createDirectories(baseDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create world storage directory: " + baseDirectory, e);
        }
    }
    
    @Override
    public void saveWorld(@NotNull String worldId, byte[] polarData) throws WorldStorageException {
        Path worldFile = getWorldPath(worldId);
        try {
            // Create parent directories if needed
            Files.createDirectories(worldFile.getParent());
            Files.write(worldFile, polarData);
        } catch (IOException e) {
            throw new WorldStorageException("Failed to save world: " + worldId, e);
        }
    }
    
    @Override
    public byte[] loadWorld(@NotNull String worldId) throws WorldStorageException {
        Path worldFile = getWorldPath(worldId);
        if (!Files.exists(worldFile)) {
            throw new WorldStorageException("World not found: " + worldId);
        }
        
        try {
            return Files.readAllBytes(worldFile);
        } catch (IOException e) {
            throw new WorldStorageException("Failed to load world: " + worldId, e);
        }
    }
    
    @Override
    public boolean exists(@NotNull String worldId) {
        return Files.exists(getWorldPath(worldId));
    }
    
    @Override
    public void deleteWorld(@NotNull String worldId) throws WorldStorageException {
        Path worldFile = getWorldPath(worldId);
        try {
            Files.deleteIfExists(worldFile);
        } catch (IOException e) {
            throw new WorldStorageException("Failed to delete world: " + worldId, e);
        }
    }
    
    /**
     * Get the full path for a world file.
     * Adds .polar extension if not present.
     */
    private Path getWorldPath(String worldId) {
        String filename = worldId.endsWith(".polar") ? worldId : worldId + ".polar";
        return baseDirectory.resolve(filename);
    }
}
