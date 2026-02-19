package org.mcuniverse.api.instance;

import net.minestom.server.instance.InstanceContainer;

import java.util.UUID;

/**
 * Represents a game world instance with metadata.
 * This is a wrapper around Minestom's InstanceContainer.
 */
public interface GameInstance {
    
    /**
     * Gets the unique name of this instance.
     * @return the instance name
     */
    String getName();
    
    /**
     * Gets the unique identifier of this instance.
     * @return the instance UUID
     */
    UUID getUuid();
    
    /**
     * Gets the underlying Minestom InstanceContainer.
     * @return the instance container
     */
    InstanceContainer getContainer();
    
    /**
     * Gets the world type of this instance.
     * @return the world type
     */
    WorldType getWorldType();
}
