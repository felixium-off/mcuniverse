package org.mcuniverse.plugins.world;

import org.jetbrains.annotations.NotNull;
import org.mcuniverse.api.instance.GameInstance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for tracking loaded game world instances.
 * Maintains a map of world names to their corresponding instances.
 */
public class WorldRegistry {
    
    private final Map<String, GameInstance> loadedWorlds = new ConcurrentHashMap<>();
    
    /**
     * Register a loaded world instance.
     * 
     * @param name Unique world name
     * @param instance The game instance
     * @throws IllegalArgumentException if a world with this name is already registered
     */
    public void register(@NotNull String name, @NotNull GameInstance instance) {
        if (loadedWorlds.containsKey(name)) {
            throw new IllegalArgumentException("World already registered: " + name);
        }
        loadedWorlds.put(name, instance);
    }
    
    /**
     * Get a world instance by name.
     * 
     * @param name World name
     * @return Optional containing the instance if found
     */
    public Optional<GameInstance> get(@NotNull String name) {
        return Optional.ofNullable(loadedWorlds.get(name));
    }
    
    /**
     * Check if a world is loaded.
     */
    public boolean isLoaded(@NotNull String name) {
        return loadedWorlds.containsKey(name);
    }
    
    /**
     * List all loaded world names.
     */
    public List<String> listWorlds() {
        return new ArrayList<>(loadedWorlds.keySet());
    }
    
    /**
     * Unload a world from the registry.
     * Note: This only removes from registry, actual instance cleanup should be done separately.
     * 
     * @param name World name
     * @return true if the world was registered and now removed
     */
    public boolean unload(@NotNull String name) {
        return loadedWorlds.remove(name) != null;
    }
    
    /**
     * Get the total number of loaded worlds.
     */
    public int getLoadedCount() {
        return loadedWorlds.size();
    }
}
