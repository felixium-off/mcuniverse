package org.mcuniverse;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.mcuniverse.economy.EconomyFactory;
import org.mcuniverse.economy.EconomyService;
import org.mcuniverse.economy.EconomyStrategy;
import org.mcuniverse.economy.commands.EconomyCommand;
import org.mcuniverse.listener.ConnectionListener;
import org.mcuniverse.island.manager.IslandManager;
import org.mcuniverse.island.commands.IslandCommand;
import org.mcuniverse.managers.SpawnManager;

public class Main {

    // 전역에서 접근 가능한 경제 서비스 인스턴스 (싱글톤처럼 활용)
    private static EconomyService economyService;

    public static void main(String[] args) {

        MinecraftServer minecraftServer = MinecraftServer.init();

        // --- [ 경제 시스템 초기화 ] ---
        // 1. 저장소 전략 선택 (메모리, DB 등 확장성을 고려한 팩토리 패턴)
        EconomyStrategy strategy = EconomyFactory.createStrategy(EconomyFactory.StorageType.MEMORY);
        // 2. 서비스 인스턴스 생성 및 전략 주입 (Dependency Injection)
        economyService = new EconomyService(strategy);

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            System.out.println("경제 시스템 데이터 정리중...");
            economyService.shutdown();
        });

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 2, Block.GRASS_BLOCK));
        instanceContainer.setChunkSupplier(LightingChunk::new);

        Pos spawnPosition = new Pos(0, 2, 0);
        SpawnManager.setSpawn(instanceContainer, spawnPosition);

        // 관리자 객체 생성
        IslandManager islandManager = new IslandManager();

        // 이벤트 리스너 등록
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();

            // 플레이어 접속 시 경제 계정 생성
            economyService.createAccount(player.getUuid());

            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 2, 0));
        });
        new ConnectionListener(globalEventHandler);

        // 명령어 등록
        MinecraftServer.getCommandManager().register(new IslandCommand(islandManager));
        MinecraftServer.getCommandManager().register(new EconomyCommand(economyService));

        minecraftServer.start("0.0.0.0", 25565);
    }

    /**
     * 초기화된 경제 서비스 인스턴스를 반환합니다.
     * @return EconomyService 객체
     */
    public static EconomyService getEconomyService() {
        return economyService;
    }
}