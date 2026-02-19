package org.mcuniverse.plugins.world.instance;

import net.minestom.server.instance.InstanceContainer;
import org.mcuniverse.api.instance.GameInstance;
import org.mcuniverse.api.instance.WorldType;

import java.util.UUID;

/**
 * Default implementation of GameInstance.
 * Follows Single Responsibility Principle (SRP) - only holds instance metadata.
 */
public class DefaultGameInstance implements GameInstance {
    
    private final String name;
    private final UUID uuid;
    private final InstanceContainer container;
    private final WorldType worldType;
    
    public DefaultGameInstance(String name, InstanceContainer container, WorldType worldType) {
        this.name = name;
        this.uuid = UUID.randomUUID();
        this.container = container;
        this.worldType = worldType;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public UUID getUuid() {
        return uuid;
    }
    
    @Override
    public InstanceContainer getContainer() {
        return container;
    }
    
    @Override
    public WorldType getWorldType() {
        return worldType;
    }
}
