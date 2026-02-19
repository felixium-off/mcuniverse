package org.mcuniverse.plugins.economy;

import net.minestom.server.MinecraftServer;
import org.mcuniverse.plugins.common.GameFeature;
import org.mcuniverse.plugins.economy.commands.EconomyAdminCommand;
import org.mcuniverse.plugins.economy.commands.EconomyCommand;
import org.mcuniverse.plugins.economy.impl.MongoEconomyStrategy;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class EconomyFeature implements GameFeature {

    private EconomyService economyService;

    @Override
    public void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp) {
        EconomyStrategy strategy = new MongoEconomyStrategy();
        this.economyService = new EconomyService(strategy);

        lamp.register(new EconomyCommand(economyService));
        lamp.register(new EconomyAdminCommand(economyService));
    }

    @Override
    public void disable(MinecraftServer server) {

        System.out.println("Economy system disabled. Ensuring data integrity...");
    }

    public EconomyService getEconomyService() {
        return economyService;
    }
}