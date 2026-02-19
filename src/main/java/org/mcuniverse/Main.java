package org.mcuniverse;

import net.minestom.server.MinecraftServer;
import org.mcuniverse.plugins.common.LampFactory;
import org.mcuniverse.plugins.common.config.ConfigManager;
import org.mcuniverse.plugins.common.database.DatabaseManager;
import org.mcuniverse.plugins.common.managers.FeatureManager;
import org.mcuniverse.plugins.economy.EconomyFeature;
import org.mcuniverse.plugins.essentials.EssentialsFeature;
import org.mcuniverse.plugins.essentials.GameModeExtension;
import org.mcuniverse.plugins.world.instance.InstanceFeature;
import org.mcuniverse.plugins.rank.RankFeature;
import org.mcuniverse.plugins.user.UserFeature;
import org.mcuniverse.plugins.world.WorldFeature;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class Main {

    static void main() {
        createServer();
    }

    private static void createServer() {
        // 설정 파일 로드
        ConfigManager.load();

        MinecraftServer minecraftServer = MinecraftServer.init();

        // FeatureManager 생성 (생명주기 관리 위임)
        FeatureManager featureManager = new FeatureManager(minecraftServer);

        // --- [ 모듈 등록 ] ---
        // InstanceFeature를 가장 먼저 등록 (다른 Feature가 의존할 수 있음)
        InstanceFeature instanceFeature = new InstanceFeature();
        featureManager.register(instanceFeature);
        
        // WorldFeature: Polar 월드 저장소 및 Room 조립 시스템
        // InstanceFeature를 전달 (enable 시점에 InstanceProvider 가져옴)
        featureManager.register(new WorldFeature(instanceFeature));
        
        RankFeature rankFeature = new RankFeature();
        EconomyFeature economyFeature = new EconomyFeature();

        featureManager.register(rankFeature);
        featureManager.register(economyFeature);
//        featureManager.register(new ShopFeature(economyFeature));
        featureManager.register(new UserFeature());
        featureManager.register(new EssentialsFeature());

        // Lamp 생성 (Factory 사용)
        Lamp<MinestomCommandActor> lamp = LampFactory.create(
                rankFeature.getRankService(),
                new GameModeExtension()
        );

        // 모든 Feature 활성화 (FeatureManager 위임)
        featureManager.enableAll(lamp);

        // 종료 작업 등록
        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            // 모든 Feature 비활성화 (FeatureManager 위임)
            featureManager.disableAll();

            // DB 연결 종료
            DatabaseManager.close();
            System.out.println("서버가 안전하게 종료되었습니다.");
        });

        minecraftServer.start("0.0.0.0", 25565);
    }
}