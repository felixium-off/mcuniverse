package org.mcuniverse.plugins.world.config;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration manager for worlds.yml
 * Handles autoload worlds and spawn points.
 */
public class WorldConfig {
    
    private static final Path CONFIG_PATH = Path.of("worlds.yml");
    
    private List<WorldEntry> autoloadWorlds = new ArrayList<>();
    private String defaultSpawnWorld = "world";
    
    /**
     * World entry in configuration
     */
    public static class WorldEntry {
        private String name;
        private String type; // "anvil" or "polar"
        private SpawnPoint spawn;
        
        public WorldEntry() {}
        
        public WorldEntry(String name, String type, SpawnPoint spawn) {
            this.name = name;
            this.type = type;
            this.spawn = spawn;
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
        public SpawnPoint getSpawn() { return spawn; }
        
        public void setName(String name) { this.name = name; }
        public void setType(String type) { this.type = type; }
        public void setSpawn(SpawnPoint spawn) { this.spawn = spawn; }
    }
    
    /**
     * Spawn point coordinates
     */
    public static class SpawnPoint {
        private double x, y, z;
        private float yaw, pitch;
        
        public SpawnPoint() {}
        
        public SpawnPoint(double x, double y, double z, float yaw, float pitch) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
        
        public static SpawnPoint fromPos(Pos pos) {
            return new SpawnPoint(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
        }
        
        public Pos toPos() {
            return new Pos(x, y, z, yaw, pitch);
        }
        
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public float getYaw() { return yaw; }
        public float getPitch() { return pitch; }
        
        public void setX(double x) { this.x = x; }
        public void setY(double y) { this.y = y; }
        public void setZ(double z) { this.z = z; }
        public void setYaw(float yaw) { this.yaw = yaw; }
        public void setPitch(float pitch) { this.pitch = pitch; }
    }
    
    /**
     * Load configuration from worlds.yml
     */
    public static WorldConfig load() throws IOException {
        if (!Files.exists(CONFIG_PATH)) {
            createDefault();
        }
        
        WorldConfig config = new WorldConfig();
        
        try {
            Yaml yaml = new Yaml();
            String content = Files.readString(CONFIG_PATH);
            Map<String, Object> data = yaml.load(content);
            
            if (data != null) {
                // Load autoload worlds
                Object autoloadObj = data.get("autoload");
                if (autoloadObj instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> map) {
                            config.autoloadWorlds.add(parseWorldEntry(map));
                        }
                    }
                }
                
                // Load default spawn world
                Object defaultWorldObj = data.get("default_spawn_world");
                if (defaultWorldObj instanceof String str) {
                    config.defaultSpawnWorld = str;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse worlds.yml: " + e.getMessage());
        }
        
        return config;
    }
    
    /**
     * Save configuration to worlds.yml
     */
    public void save() throws IOException {
        Map<String, Object> data = new LinkedHashMap<>();
        
        // Convert autoload worlds
        List<Map<String, Object>> autoloadList = new ArrayList<>();
        for (WorldEntry entry : autoloadWorlds) {
            Map<String, Object> entryMap = new LinkedHashMap<>();
            entryMap.put("name", entry.name);
            entryMap.put("type", entry.type);
            
            if (entry.spawn != null) {
                Map<String, Object> spawnMap = new LinkedHashMap<>();
                spawnMap.put("x", entry.spawn.x);
                spawnMap.put("y", entry.spawn.y);
                spawnMap.put("z", entry.spawn.z);
                spawnMap.put("yaw", entry.spawn.yaw);
                spawnMap.put("pitch", entry.spawn.pitch);
                entryMap.put("spawn", spawnMap);
            }
            
            autoloadList.add(entryMap);
        }
        
        data.put("autoload", autoloadList);
        data.put("default_spawn_world", defaultSpawnWorld);
        
        Yaml yaml = new Yaml();
        String output = yaml.dump(data);
        Files.writeString(CONFIG_PATH, output);
    }
    
    /**
     * Create default configuration file
     */
    private static void createDefault() throws IOException {
        String defaultConfig = """
            # Auto-load worlds on server start
            autoload: []
            
            # Default spawn world for new players
            default_spawn_world: "world"
            """;
        
        Files.writeString(CONFIG_PATH, defaultConfig);
    }
    
    /**
     * Parse a world entry from YAML map
     */
    private static WorldEntry parseWorldEntry(Map<?, ?> map) {
        WorldEntry entry = new WorldEntry();
        
        Object name = map.get("name");
        if (name instanceof String str) {
            entry.name = str;
        }
        
        Object type = map.get("type");
        if (type instanceof String str) {
            entry.type = str;
        }
        
        Object spawn = map.get("spawn");
        if (spawn instanceof Map<?, ?> spawnMap) {
            entry.spawn = parseSpawnPoint(spawnMap);
        }
        
        return entry;
    }
    
    /**
     * Parse spawn point from YAML map
     */
    private static SpawnPoint parseSpawnPoint(Map<?, ?> map) {
        SpawnPoint spawn = new SpawnPoint();
        
        Object x = map.get("x");
        if (x instanceof Number num) spawn.x = num.doubleValue();
        
        Object y = map.get("y");
        if (y instanceof Number num) spawn.y = num.doubleValue();
        
        Object z = map.get("z");
        if (z instanceof Number num) spawn.z = num.doubleValue();
        
        Object yaw = map.get("yaw");
        if (yaw instanceof Number num) spawn.yaw = num.floatValue();
        
        Object pitch = map.get("pitch");
        if (pitch instanceof Number num) spawn.pitch = num.floatValue();
        
        return spawn;
    }
    
    // Getters
    public List<WorldEntry> getAutoloadWorlds() {
        return autoloadWorlds;
    }
    
    public String getDefaultSpawnWorld() {
        return defaultSpawnWorld;
    }
    
    public void setDefaultSpawnWorld(String worldName) {
        this.defaultSpawnWorld = worldName;
    }
    
    /**
     * Check if a world exists in autoload list
     */
    public boolean hasWorld(@NotNull String worldName) {
        return autoloadWorlds.stream()
            .anyMatch(entry -> entry.name.equals(worldName));
    }
    
    /**
     * Add a world to autoload list
     */
    public void addWorld(@NotNull String name, @NotNull String type, @Nullable SpawnPoint spawn) {
        if (!hasWorld(name)) {
            autoloadWorlds.add(new WorldEntry(name, type, spawn));
        }
    }
    
    /**
     * Update spawn point for a world
     */
    public void updateSpawn(@NotNull String worldName, @NotNull Pos pos) {
        for (WorldEntry entry : autoloadWorlds) {
            if (entry.name.equals(worldName)) {
                entry.spawn = SpawnPoint.fromPos(pos);
                return;
            }
        }
        
        // If world doesn't exist, add it
        addWorld(worldName, "anvil", SpawnPoint.fromPos(pos));
    }
    
    /**
     * Get spawn point for a world
     */
    @Nullable
    public SpawnPoint getSpawn(@NotNull String worldName) {
        return autoloadWorlds.stream()
            .filter(entry -> entry.name.equals(worldName))
            .map(entry -> entry.spawn)
            .findFirst()
            .orElse(null);
    }
}
