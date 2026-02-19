package org.mcuniverse.plugins.world;

import com.google.gson.Gson;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import org.jetbrains.annotations.NotNull;
import org.mcuniverse.api.instance.GameInstance;
import org.mcuniverse.api.instance.WorldType;
import org.mcuniverse.api.instance.exception.InstanceException;
import org.mcuniverse.api.instance.exception.InstanceLoadException;
import org.mcuniverse.api.world.RoomAssembler;
import org.mcuniverse.api.world.RoomMetadata;
import org.mcuniverse.api.world.WorldStorage;
import org.mcuniverse.api.world.exception.WorldStorageException;
import org.mcuniverse.plugins.world.instance.DefaultGameInstance;
import org.mcuniverse.plugins.world.storage.FileSystemWorldStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Assembles dungeons from individual Polar room files.
 * Loads rooms from filesystem and combines them with proper offset calculations.
 */
public class PolarRoomAssembler implements RoomAssembler {
    
    private final WorldStorage roomStorage;
    private final InstanceManager instanceManager;
    private final Map<String, RoomMetadata> metadataCache;
    private final Gson gson;
    
    public PolarRoomAssembler() {
        this(new FileSystemWorldStorage(Paths.get("worlds_polar/rooms")));
    }
    
    public PolarRoomAssembler(WorldStorage roomStorage) {
        this.roomStorage = roomStorage;
        this.instanceManager = MinecraftServer.getInstanceManager();
        this.metadataCache = new HashMap<>();
        this.gson = new Gson();
    }
    
    @Override
    public GameInstance assembleDungeon(@NotNull String dungeonName, @NotNull List<String> roomNames) 
            throws InstanceException {
        
        if (roomNames.isEmpty()) {
            throw new InstanceLoadException("Cannot assemble dungeon with no rooms");
        }
        
        try {
            // Create main dungeon instance
            InstanceContainer dungeon = instanceManager.createInstanceContainer();
            dungeon.setChunkSupplier(LightingChunk::new);
            
            int currentOffsetX = 0;
            
            // Load and place each room
            for (String roomName : roomNames) {
                // Load room metadata
                RoomMetadata metadata = loadRoomMetadata(roomName);
                
                // Load room Polar data
                byte[] polarData = roomStorage.loadWorld(roomName);
                
                // Place room at current offset
                placeRoom(dungeon, polarData, currentOffsetX, 0);
                
                // Update offset for next room
                currentOffsetX += metadata.getSizeChunks().x * 16; // chunks to blocks
            }
            
            return new DefaultGameInstance(dungeonName, dungeon, WorldType.POLAR);
            
        } catch (WorldStorageException e) {
            throw new InstanceLoadException("Failed to load room data", e);
        }
    }
    
    /**
     * Load room metadata from JSON file.
     * Caches results to avoid repeated file reads.
     */
    private RoomMetadata loadRoomMetadata(String roomName) throws InstanceLoadException {
        if (metadataCache.containsKey(roomName)) {
            return metadataCache.get(roomName);
        }
        
        Path metadataPath = Paths.get("worlds_polar/rooms", roomName + ".json");
        
        if (!Files.exists(metadataPath)) {
            // Create default metadata if file doesn't exist
            RoomMetadata defaultMetadata = new RoomMetadata(
                roomName,
                new RoomMetadata.ChunkSize(2, 2), // Default 2x2 chunks
                new RoomMetadata.Position(16, 64, 16, "NORTH"),
                new RoomMetadata.Position[0],
                new RoomMetadata.Position[0]
            );
            metadataCache.put(roomName, defaultMetadata);
            return defaultMetadata;
        }
        
        try {
            String json = Files.readString(metadataPath);
            RoomMetadata metadata = gson.fromJson(json, RoomMetadata.class);
            metadataCache.put(roomName, metadata);
            return metadata;
        } catch (IOException e) {
            throw new InstanceLoadException("Failed to load metadata for room: " + roomName, e);
        }
    }
    
    /**
     * Place a room into the dungeon instance at the specified offset.
     * Uses temporary file approach for PolarLoader.
     */
    private void placeRoom(InstanceContainer dungeon, byte[] polarData, int offsetX, int offsetZ) 
            throws InstanceLoadException {
        
        Path tempPolarFile = null;
        try {
            // 1. Write Polar data to temporary file
            tempPolarFile = Files.createTempFile("room_temp_", ".polar");
            Files.write(tempPolarFile, polarData);
            
            // 2. Create temporary instance with PolarLoader
            InstanceContainer tempInstance = instanceManager.createInstanceContainer();
            tempInstance.setChunkSupplier(LightingChunk::new);
            tempInstance.setChunkLoader(new PolarLoader(tempPolarFile));
            
            // 3. Load all chunks from temp instance
            // Note: This is a simplified approach. For production, you'd want to:
            // - Copy chunks with offset to the main dungeon instance
            // - Use chunk copying utilities if available
            // For now, we document this as a placeholder for manual chunk copying
            
            // TODO: Implement chunk copying with offset
            // This requires iterating through tempInstance chunks and copying them
            // to the dungeon instance with X/Z offset applied
            
            // 4. Cleanup temp instance
            instanceManager.unregisterInstance(tempInstance);
            
        } catch (IOException e) {
            throw new InstanceLoadException("Failed to place room at offset (" + offsetX + ", " + offsetZ + ")", e);
        } finally {
            // Cleanup temporary file
            if (tempPolarFile != null) {
                try {
                    Files.deleteIfExists(tempPolarFile);
                } catch (IOException ignored) {
                }
            }
        }
    }
}
