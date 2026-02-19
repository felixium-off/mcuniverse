package org.mcuniverse.api.world;

import org.jetbrains.annotations.NotNull;

/**
 * Metadata for a dungeon room.
 * Contains size, spawn points, and entrance/exit locations.
 */
public class RoomMetadata {
    
    private final String name;
    private final ChunkSize sizeChunks;
    private final Position spawnPoint;
    private final Position[] entrances;
    private final Position[] exits;
    
    public RoomMetadata(@NotNull String name, 
                        @NotNull ChunkSize sizeChunks,
                        @NotNull Position spawnPoint,
                        @NotNull Position[] entrances,
                        @NotNull Position[] exits) {
        this.name = name;
        this.sizeChunks = sizeChunks;
        this.spawnPoint = spawnPoint;
        this.entrances = entrances;
        this.exits = exits;
    }
    
    public String getName() {
        return name;
    }
    
    public ChunkSize getSizeChunks() {
        return sizeChunks;
    }
    
    public Position getSpawnPoint() {
        return spawnPoint;
    }
    
    public Position[] getEntrances() {
        return entrances;
    }
    
    public Position[] getExits() {
        return exits;
    }
    
    /**
     * Size in chunks (16x16 blocks per chunk).
     */
    public static class ChunkSize {
        public final int x;
        public final int z;
        
        public ChunkSize(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }
    
    /**
     * 3D position with direction.
     */
    public static class Position {
        public final int x;
        public final int y;
        public final int z;
        public final String direction; // NORTH, SOUTH, EAST, WEST
        
        public Position(int x, int y, int z, String direction) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.direction = direction;
        }
    }
}
