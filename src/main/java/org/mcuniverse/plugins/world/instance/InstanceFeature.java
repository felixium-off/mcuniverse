package org.mcuniverse.plugins.world.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import org.mcuniverse.api.instance.GameInstance;
import org.mcuniverse.api.instance.InstanceProvider;
import org.mcuniverse.api.instance.WorldType;
import org.mcuniverse.api.instance.exception.InstanceException;
import org.mcuniverse.plugins.common.GameFeature;
import org.mcuniverse.plugins.common.managers.SpawnManager;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

/**
 * Feature that manages game instances and handles default world creation.
 * Follows Single Responsibility Principle - only handles instance lifecycle.
 */
public class InstanceFeature implements GameFeature {
    
    private static final String DEFAULT_WORLD_NAME = "world";
    private static final Pos DEFAULT_SPAWN = new Pos(0, 2, 0);
    
    private InstanceProvider instanceProvider;
    private GameInstance defaultWorld;
    
    /**
     * Registers the player spawn handler.
     * Players will spawn in the configured default spawn world.
     */
    private void registerPlayerSpawnHandler() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        
        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            
            // Get configured spawn world from SpawnManager
            String spawnWorldName = SpawnManager.getDefaultSpawnWorld();
            Pos spawnPos = SpawnManager.getRegisteredSpawn(spawnWorldName);
            
            // Try to get the configured spawn world instance
            GameInstance spawnWorld = instanceProvider.getInstance(spawnWorldName).orElse(null);
            
            if (spawnWorld != null && spawnPos != null) {
                // Use configured spawn
                event.setSpawningInstance(spawnWorld.getContainer());
                player.setRespawnPoint(spawnPos);
            } else {
                // Fallback to default
                event.setSpawningInstance(defaultWorld.getContainer());
                player.setRespawnPoint(DEFAULT_SPAWN);
                
                if (spawnWorld == null) {
                    System.out.println("[InstanceFeature] Spawn world '" + spawnWorldName + "' not found, using default");
                }
            }
        });
    }
    
    /**
     * Gets the InstanceProvider for other features to use.
     * @return the instance provider
     */
    public InstanceProvider getInstanceProvider() {
        return instanceProvider;
    }
    
    /**
     * Gets the default world.
     * @return the default world instance
     */
    public GameInstance getDefaultWorld() {
        return defaultWorld;
    }

    @Override
    public void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp) {
        // Create the provider
        this.instanceProvider = new MinestomInstanceProvider();

        try {
            // Create a default spawn world
            this.defaultWorld = instanceProvider.createInstance(DEFAULT_WORLD_NAME, WorldType.FLAT);

            // Set spawn point
            SpawnManager.setSpawn(defaultWorld.getContainer(), DEFAULT_SPAWN);

            // Register player join listener
            registerPlayerSpawnHandler();

            System.out.println("[InstanceFeature] Default world '" + DEFAULT_WORLD_NAME + "' created.");

        } catch (InstanceException e) {
            throw new RuntimeException("Failed to create default world", e);
        }
    }

    @Override
    public void disable(MinecraftServer server) {
        System.out.println("[InstanceFeature] Instance system disabled.");
    }
}
