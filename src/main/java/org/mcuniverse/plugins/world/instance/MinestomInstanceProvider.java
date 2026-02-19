package org.mcuniverse.plugins.world.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.mcuniverse.api.instance.GameInstance;
import org.mcuniverse.api.instance.InstanceProvider;
import org.mcuniverse.api.instance.WorldType;
import org.mcuniverse.api.instance.exception.InstanceAlreadyExistsException;
import org.mcuniverse.api.instance.exception.InstanceLoadException;
import org.mcuniverse.api.instance.exception.InstanceNotFoundException;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minestom-specific implementation of InstanceProvider.
 * Follows SOLID principles:
 * - SRP: Only manages instance lifecycle
 * - OCP: Extendable via WorldType enum
 * - DIP: Depends on abstractions (GameInstance interface)
 */
public class MinestomInstanceProvider implements InstanceProvider {
    
    private final InstanceManager instanceManager;
    private final Map<String, GameInstance> instances;
    
    public MinestomInstanceProvider() {
        this.instanceManager = MinecraftServer.getInstanceManager();
        this.instances = new ConcurrentHashMap<>();
    }
    
    @Override
    public GameInstance createInstance(String name, WorldType type) 
            throws InstanceAlreadyExistsException, InstanceLoadException {
        
        // Check for duplicates
        if (instances.containsKey(name)) {
            throw new InstanceAlreadyExistsException(name);
        }
        
        try {
            // Create Minestom instance
            InstanceContainer container = instanceManager.createInstanceContainer();
            
            // Apply world generation based on type
            applyWorldGeneration(container, type);
            
            // Wrap in our GameInstance
            GameInstance gameInstance = new DefaultGameInstance(name, container, type);
            instances.put(name, gameInstance);
            
            return gameInstance;
            
        } catch (Exception e) {
            throw new InstanceLoadException("Failed to create instance: " + name, e);
        }
    }
    
    @Override
    public Optional<GameInstance> getInstance(String name) {
        return Optional.ofNullable(instances.get(name));
    }
    
    @Override
    public List<GameInstance> getAllInstances() {
        return List.copyOf(instances.values());
    }
    
    @Override
    public boolean unloadInstance(String name) throws InstanceNotFoundException {
        GameInstance instance = instances.remove(name);
        
        if (instance == null) {
            throw new InstanceNotFoundException(name);
        }
        
        // Unregister from Minestom
        instanceManager.unregisterInstance(instance.getContainer());
        return true;
    }
    
    @Override
    public GameInstance createInstanceFromPolar(String name, byte[] polarData) 
            throws InstanceAlreadyExistsException, InstanceLoadException {
        
        // Check for duplicates
        if (instances.containsKey(name)) {
            throw new InstanceAlreadyExistsException(name);
        }
        
        Path tempPolarFile = null;
        try {
            // 1. Create temporary Polar file
            tempPolarFile = java.nio.file.Files.createTempFile("instance_" + name, ".polar");
            java.nio.file.Files.write(tempPolarFile, polarData);
            
            // 2. Create instance with PolarLoader
            InstanceContainer container = instanceManager.createInstanceContainer();
            container.setChunkSupplier(net.minestom.server.instance.LightingChunk::new);
            container.setChunkLoader(new net.hollowcube.polar.PolarLoader(tempPolarFile));
            
            // 3. Wrap in GameInstance
            GameInstance gameInstance = new DefaultGameInstance(name, container, WorldType.POLAR);
            instances.put(name, gameInstance);
            
            return gameInstance;
            
        } catch (java.io.IOException e) {
            throw new InstanceLoadException("Failed to create Polar instance: " + name, e);
        } finally {
            // 4. Clean up temporary file
            if (tempPolarFile != null) {
                try {
                    java.nio.file.Files.deleteIfExists(tempPolarFile);
                } catch (java.io.IOException ignored) {
                    // Cleanup failure is non-critical
                }
            }
        }
    }
    
    /**
     * Applies world generation strategy based on WorldType.
     * Follows Open/Closed Principle - add new types without modifying existing code.
     */
    private void applyWorldGeneration(InstanceContainer container, WorldType type) {
        container.setChunkSupplier(LightingChunk::new);
        
        switch (type) {
            case VOID:
                // No generator - completely empty
                break;
                
            case FLAT:
                container.setGenerator(unit -> 
                    unit.modifier().fillHeight(0, 2, Block.GRASS_BLOCK)
                );
                break;
                
            case NORMAL:
                // Placeholder for future terrain generation
                container.setGenerator(unit -> 
                    unit.modifier().fillHeight(0, 1, Block.STONE)
                );
                break;
        }
    }
}
