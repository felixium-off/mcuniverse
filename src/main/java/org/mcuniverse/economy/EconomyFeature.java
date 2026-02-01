package org.mcuniverse.economy;

import net.minestom.server.MinecraftServer;
import org.mcuniverse.common.GameFeature;
import org.mcuniverse.economy.commands.EconomyCommand;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class EconomyFeature implements GameFeature {

    private EconomyService economyService;

    @Override
    public void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp) {
        // 1. 전략 및 서비스 초기화
        EconomyStrategy strategy = EconomyFactory.createStrategy(EconomyFactory.StorageType.MEMORY);
        this.economyService = new EconomyService(strategy);

        // 2. 명령어 등록
        lamp.register(new EconomyCommand(economyService));
        
        // 3. 이벤트 리스너 (접속 시 계정 생성)
        server.getGlobalEventHandler().addListener(net.minestom.server.event.player.AsyncPlayerConfigurationEvent.class, event -> {
            economyService.createAccount(event.getPlayer().getUuid());
        });
    }

    @Override
    public void disable(MinecraftServer server) {
        if (economyService != null) {
            economyService.shutdown();
        }
    }
}