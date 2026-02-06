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
import org.mcuniverse.common.GameFeature;
import org.mcuniverse.common.LampFactory;
import org.mcuniverse.common.config.ConfigManager;
import org.mcuniverse.common.database.DatabaseManager;
import org.mcuniverse.economy.EconomyFeature;
import org.mcuniverse.essentials.EssentialsFeature;
import org.mcuniverse.essentials.GameModeExtension;
import org.mcuniverse.common.managers.SpawnManager;
import org.mcuniverse.rank.RankFeature;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final List<GameFeature> features = new ArrayList<>();

    static void main() {
        createServer();
    }

    private static void createServer() {
        // 설정 파일 로드
        ConfigManager.load();

        MinecraftServer minecraftServer = MinecraftServer.init();

        // 랭크 기능 미리 초기화 (권한 처리를 위해 Service가 필요함)
        RankFeature rankFeature = new RankFeature();
        rankFeature.enable(minecraftServer, null); // Service 생성 (Lamp는 null)

        // 인스턴스 설정
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 2, Block.GRASS_BLOCK));
        instanceContainer.setChunkSupplier(LightingChunk::new);

        Pos spawnPosition = new Pos(0, 2, 0);
        SpawnManager.setSpawn(instanceContainer, spawnPosition);

        // 이벤트 리스너 등록
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 2, 0));
        });

        // --- [ 모듈 등록 및 초기화 ] ---
        features.add(rankFeature);
        features.add(new EconomyFeature());
//        features.add(new org.mcuniverse.shop.ShopFeature()); // Shop 등록
        features.add(new EssentialsFeature());

        // Lamp 생성 (Factory 사용)
        Lamp<MinestomCommandActor> lamp = LampFactory.create(
                rankFeature.getRankService(),
                new GameModeExtension() // 게임모드 관련 설정(파라미터, 자동완성) 주입
        );

        // 나머지 기능 활성화
        for (GameFeature feature : features) {
            feature.enable(minecraftServer, lamp);
        }

        // 종료 작업 등록
        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            for (GameFeature feature : features) {
                feature.disable(minecraftServer);
            }
            
            // DB 연결 종료
            DatabaseManager.close();
            System.out.println("서버가 안전하게 종료되었습니다.");
        });

        minecraftServer.start("0.0.0.0", 25565);
    }
}