package org.mcuniverse.api.world;

import org.jetbrains.annotations.NotNull;
import org.mcuniverse.api.instance.GameInstance;
import org.mcuniverse.api.instance.exception.InstanceException;

import java.util.List;

/**
 * Interface for assembling dungeon instances from individual room components.
 * Rooms are loaded from the filesystem and combined at runtime.
 */
public interface RoomAssembler {
    
    /**
     * Assemble a dungeon from a list of room names.
     * Rooms are placed sequentially with proper offset calculations.
     * 
     * @param dungeonName Unique name for the assembled dungeon instance
     * @param roomNames List of room names (without .polar extension)
     * @return Assembled dungeon instance
     * @throws InstanceException if assembly fails or rooms not found
     */
    GameInstance assembleDungeon(@NotNull String dungeonName, @NotNull List<String> roomNames) 
            throws InstanceException;
}
