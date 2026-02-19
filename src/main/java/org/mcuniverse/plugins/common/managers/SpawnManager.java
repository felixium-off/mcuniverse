package org.mcuniverse.plugins.common.managers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import org.mcuniverse.plugins.world.config.WorldConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnManager {
    private static InstanceContainer spawnInstance;
    private static Pos spawnPosition;
    
    // Global spawn registry (world name -> spawn position)
    private static final Map<String, Pos> spawnRegistry = new ConcurrentHashMap<>();
    private static String defaultSpawnWorld = "world";
    
    public static void setSpawn(InstanceContainer instance, Pos position) {
        spawnInstance = instance;
        spawnPosition = position;
    }
    
    public static InstanceContainer getSpawnInstance() {
        return spawnInstance;
    }
    
    public static Pos getSpawnPosition() {
        return spawnPosition != null ? spawnPosition : new Pos(0, 2, 0);
    }
    
    /**
     * 플레이어를 메인 spawn으로 이동시킵니다.
     */
    public static void teleportToSpawn(Player player) {
        if (spawnInstance != null) {
            // 현재 인스턴스와 이동하려는 인스턴스가 다른 경우에만 setInstance 호출
            if (player.getInstance() != spawnInstance) {
                player.setInstance(spawnInstance);
            }
            // 같은 인스턴스에 있으면 teleport만 호출
            player.teleport(getSpawnPosition());
        }
    }
    
    // === New spawn registry methods ===
    
    /**
     * Register a spawn point by world name (persists across instance reloads)
     */
    public static void registerSpawn(String worldName, Pos spawn) {
        spawnRegistry.put(worldName, spawn);
    }
    
    /**
     * Get registered spawn for a world
     */
    public static Pos getRegisteredSpawn(String worldName) {
        return spawnRegistry.get(worldName);
    }
    
    /**
     * Load spawn registry from WorldConfig
     */
    public static void loadFromConfig(WorldConfig config) {
        config.getAutoloadWorlds().forEach(entry -> {
            if (entry.getSpawn() != null) {
                spawnRegistry.put(entry.getName(), entry.getSpawn().toPos());
            }
        });
    }
    
    /**
     * Get default spawn world name
     */
    public static String getDefaultSpawnWorld() {
        return defaultSpawnWorld;
    }
    
    /**
     * Set default spawn world name
     */
    public static void setDefaultSpawnWorld(String worldName) {
        defaultSpawnWorld = worldName;
    }
}