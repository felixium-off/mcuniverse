package org.mcuniverse.plugins.cosmetics;

import net.minestom.server.MinecraftServer;
import org.mcuniverse.plugins.common.GameFeature;
import org.mcuniverse.plugins.common.config.ConfigManager;
import org.mcuniverse.plugins.cosmetics.api.CosmeticType;
import org.mcuniverse.plugins.cosmetics.impl.chat.ColorChatCosmetic;
import org.mcuniverse.plugins.cosmetics.impl.wardrobe.WardrobeCosmetic;
import org.mcuniverse.plugins.cosmetics.listener.CosmeticListener;
import org.mcuniverse.plugins.cosmetics.manager.CosmeticManager;
import org.mcuniverse.plugins.cosmetics.registry.CosmeticRegistry;
import org.mcuniverse.plugins.cosmetics.storage.CosmeticRepository;
import org.mcuniverse.plugins.cosmetics.storage.MongoCosmeticRepository;
import org.mcuniverse.plugins.cosmetics.storage.JsonCosmeticRepository;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

import java.nio.file.Path;

public class DecoratorFeature implements GameFeature {

    private CosmeticRegistry registry;
    private CosmeticManager manager;

    @Override
    public void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp) {
        this.registry = new CosmeticRegistry();
        
        // 설정에서 저장소 타입 로드
        String storageType = ConfigManager.get("storage.cosmetics.type", "JSON").toUpperCase();
        CosmeticRepository repository;

        if (storageType.equals("MONGODB")) {
            repository = new MongoCosmeticRepository();
        } else {
            // 기본값 또는 JSON
            repository = new JsonCosmeticRepository(Path.of("cosmetics"));
        }
        
        this.manager = new CosmeticManager(registry, repository);

        // 기본 치장품 등록
        // 예시: 빨간색 채팅
        registry.register(new ColorChatCosmetic("chat_red", net.kyori.adventure.text.format.NamedTextColor.RED));
        
        // 예시: 블록벤치 모자 (Model ID: 1001)
        registry.register(new WardrobeCosmetic("hat_viking", "바이킹 투구", CosmeticType.WARDROBE_HAT, 1001));

        // 리스너 및 명령어 등록
        new CosmeticListener(manager, server.getGlobalEventHandler());

        if (lamp != null) {
            // lamp.register(new CosmeticCommand(manager));
        }
    }

    @Override
    public void disable(MinecraftServer server) {
        // 모든 플레이어의 치장 효과 해제 (파티클 스레드 종료 등)
        manager.shutdown();
    }

    public CosmeticManager getManager() {
        return manager;
    }
}