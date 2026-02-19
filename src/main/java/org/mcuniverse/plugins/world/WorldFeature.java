package org.mcuniverse.plugins.world;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import net.minestom.server.MinecraftServer;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.mcuniverse.api.instance.InstanceProvider;
import org.mcuniverse.api.world.RoomAssembler;
import org.mcuniverse.api.world.WorldStorage;
import org.mcuniverse.plugins.common.GameFeature;
import org.mcuniverse.plugins.world.command.WorldCommand;
import org.mcuniverse.plugins.world.instance.InstanceFeature;
import org.mcuniverse.plugins.world.storage.FileSystemWorldStorage;
import org.mcuniverse.plugins.world.storage.MongoWorldStorage;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Feature for managing world storage systems.
 * Initializes filesystem and MongoDB storage backends.
 */
public class WorldFeature implements GameFeature {

    private static final String ROOMS_DIR = "worlds_polar/rooms";
    private static final String TEMPLATES_DIR = "worlds_polar/templates";

    private WorldStorage fileSystemStorage;
    private WorldStorage mongoStorage;
    private RoomAssembler roomAssembler;
    private WorldRegistry worldRegistry;
    private InstanceFeature instanceFeature;

    /**
     * Create WorldFeature with dependency injection.
     * 
     * @param instanceFeature InstanceFeature to get InstanceProvider from during enable()
     */
    public WorldFeature(InstanceFeature instanceFeature) {
        this.instanceFeature = instanceFeature;
    }

    @NotNull
    @Override
    public void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp) {
        MinecraftServer.LOGGER.info("Enabling WorldFeature...");

        // 1. Verify and create directories
        verifyDirectories();

        // 2. Initialize filesystem storage for dungeon rooms
        fileSystemStorage = new FileSystemWorldStorage(Paths.get(ROOMS_DIR));
        MinecraftServer.LOGGER.info("Initialized filesystem storage for dungeon rooms");

        // 3. Initialize MongoDB storage for player islands
        String mongoUri = System.getenv("MONGODB_URI");
        if (mongoUri != null && !mongoUri.isEmpty()) {
            try {
                MongoClient mongoClient = MongoClients.create(mongoUri);
                MongoCollection<Document> islandsCollection = mongoClient
                        .getDatabase("mcuniverse")
                        .getCollection("islands");

                mongoStorage = new MongoWorldStorage(islandsCollection);
                MinecraftServer.LOGGER.info("Initialized MongoDB storage for player islands");
            } catch (Exception e) {
                MinecraftServer.LOGGER.error("Failed to initialize MongoDB storage", e);
                MinecraftServer.LOGGER.warn("Player island persistence will be unavailable");
            }
        } else {
            MinecraftServer.LOGGER.warn("MONGODB_URI not set - player island persistence disabled");
        }

        // 4. Initialize room assembler
        roomAssembler = new PolarRoomAssembler(fileSystemStorage);
        MinecraftServer.LOGGER.info("Initialized room assembler for dungeon generation");

        // 5. Initialize world registry
        worldRegistry = new WorldRegistry();
        MinecraftServer.LOGGER.info("Initialized world registry");

        // 6. Load world configuration and autoload worlds
        try {
            org.mcuniverse.plugins.world.config.WorldConfig config = 
                org.mcuniverse.plugins.world.config.WorldConfig.load();
            
            // Load spawn points into SpawnManager
            org.mcuniverse.plugins.common.managers.SpawnManager.loadFromConfig(config);
            org.mcuniverse.plugins.common.managers.SpawnManager.setDefaultSpawnWorld(
                config.getDefaultSpawnWorld()
            );
            
            MinecraftServer.LOGGER.info("Loaded world configuration");
            
            // Autoload configured worlds
            for (org.mcuniverse.plugins.world.config.WorldConfig.WorldEntry entry : 
                    config.getAutoloadWorlds()) {
                try {
                    autoloadWorld(entry);
                    MinecraftServer.LOGGER.info("Auto-loaded world: " + entry.getName());
                } catch (Exception e) {
                    MinecraftServer.LOGGER.error("Failed to autoload world: " + entry.getName(), e);
                }
            }
            
        } catch (Exception e) {
            MinecraftServer.LOGGER.warn("No world configuration found, using defaults: " + e.getMessage());
        }

        // 7. Register world admin commands
        // Get InstanceProvider (now initialized after enable())
        InstanceProvider instanceProvider = instanceFeature.getInstanceProvider();
        
        WorldCommand worldCommand = new WorldCommand(
            worldRegistry,
            instanceProvider,
            fileSystemStorage
        );
        lamp.register(worldCommand);
        MinecraftServer.LOGGER.info("Registered world admin commands");

        MinecraftServer.LOGGER.info("WorldFeature enabled successfully");
    }

    @Override
    public void disable(MinecraftServer server) {
        MinecraftServer.LOGGER.info("Disabling WorldFeature...");
    }

    /**
     * Verify required directories exist, create if missing.
     */
    private void verifyDirectories() {
        try {
            Path roomsPath = Paths.get(ROOMS_DIR);
            Path templatesPath = Paths.get(TEMPLATES_DIR);

            if (!Files.exists(roomsPath)) {
                Files.createDirectories(roomsPath);
                MinecraftServer.LOGGER.info("Created directory: " + ROOMS_DIR);
            }

            if (!Files.exists(templatesPath)) {
                Files.createDirectories(templatesPath);
                MinecraftServer.LOGGER.info("Created directory: " + TEMPLATES_DIR);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to verify world directories", e);
        }
    }
    
    /**
     * Auto-load a world from configuration
     */
    private void autoloadWorld(org.mcuniverse.plugins.world.config.WorldConfig.WorldEntry entry) throws Exception {
        byte[] polarData;
        
        // Load world data based on type
        if ("anvil".equals(entry.getType())) {
            polarData = AnvilWorldImporter.importAnvilWorld(entry.getName());
        } else if ("polar".equals(entry.getType())) {
            polarData = fileSystemStorage.loadWorld("templates/" + entry.getName());
        } else {
            throw new IllegalArgumentException("Unknown world type: " + entry.getType());
        }
        
        // Create instance
        InstanceProvider instanceProvider = instanceFeature.getInstanceProvider();
        org.mcuniverse.api.instance.GameInstance instance = 
            instanceProvider.createInstanceFromPolar(entry.getName(), polarData);
        
        // Register in WorldRegistry
        worldRegistry.register(entry.getName(), instance);
        
        // Set spawn point if configured
        if (entry.getSpawn() != null) {
            org.mcuniverse.plugins.common.managers.SpawnManager.setSpawn(
                instance.getContainer(),
                entry.getSpawn().toPos()
            );
            org.mcuniverse.plugins.common.managers.SpawnManager.registerSpawn(
                entry.getName(),
                entry.getSpawn().toPos()
            );
        }
    }

    /**
     * Get the filesystem storage (for dungeon rooms).
     */
    public WorldStorage getFileSystemStorage() {
        return fileSystemStorage;
    }

    /**
     * Get the MongoDB storage (for player islands).
     * May be null if MongoDB is not configured.
     */
    public WorldStorage getMongoStorage() {
        return mongoStorage;
    }

    /**
     * Get the room assembler for building dungeons.
     */
    public RoomAssembler getRoomAssembler() {
        return roomAssembler;
    }
    
    /**
     * Get the world registry for tracking loaded worlds.
     */
    public WorldRegistry getWorldRegistry() {
        return worldRegistry;
    }
}
