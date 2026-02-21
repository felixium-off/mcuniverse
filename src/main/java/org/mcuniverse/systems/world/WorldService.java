package org.mcuniverse.systems.world;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WorldService {

    private final Map<String, InstanceContainer> instances = new HashMap<>();

    public InstanceContainer createWorld(String name) {
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        instances.put(name, instanceContainer);

        return instanceContainer;
    }

    public Optional<InstanceContainer> getWorld(String name) {
        return Optional.ofNullable(instances.get(name));
    }

    public InstanceContainer loadPolarWorld(String name, byte[] polarData) {
        return getWorld(name).orElseGet(() -> createWorld(name));
    }
}
