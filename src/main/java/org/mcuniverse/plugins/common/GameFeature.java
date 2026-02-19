package org.mcuniverse.plugins.common;

import net.minestom.server.MinecraftServer;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public interface GameFeature {
    void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp);
    void disable(MinecraftServer server);
}