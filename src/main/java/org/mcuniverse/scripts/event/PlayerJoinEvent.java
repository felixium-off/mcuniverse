package org.mcuniverse.scripts.event;

import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import org.mcuniverse.systems.resourcepack.ResourcepackService;
import org.mcuniverse.systems.world.WorldService;

public class PlayerJoinEvent {

    private WorldService worldService;
    private ResourcepackService resourcepackService;

    public PlayerJoinEvent(WorldService worldService, ResourcepackService resourcepackService) {
        this.worldService = worldService;
        this.resourcepackService = resourcepackService;
    }

    public void register(GlobalEventHandler eventNode) {
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, this::onAsyncPlayerConfiguration);
        eventNode.addListener(PlayerSpawnEvent.class, this::onPlayerSpawn);
    }

    // 플레이어 비동기 로그인 이벤트 (로그인 핸드셰이크)
    private void onAsyncPlayerConfiguration(AsyncPlayerConfigurationEvent event) {
        InstanceContainer instanceContainer = worldService.createWorld("lobby");
        event.setSpawningInstance(instanceContainer);
        event.getPlayer().setRespawnPoint(new Pos(0, 42, 0));
    }

    // 플레이어 스폰 시 실행될 로직
    private void onPlayerSpawn(PlayerSpawnEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Component.text("마인크래프트 유니버스에 오신 것을 환영합니다!", NamedTextColor.AQUA));

        loadResourcePack(player);
    }

    private void loadResourcePack(Player player) {
        ResourcePackRequest request = resourcepackService.getRequest().callback(((uuid, status, audience) -> {
            if (status == ResourcePackStatus.SUCCESSFULLY_LOADED) {
                player.sendMessage(Component.text("✅ 리소스팩 로딩 완료!", NamedTextColor.GREEN));
            } else if (status == ResourcePackStatus.FAILED_DOWNLOAD || status == ResourcePackStatus.DECLINED) {
                player.kick(Component.text("원활한 플레이를 위해 커스텀 리소스팩 적용이 필수입니다.", NamedTextColor.RED));
            }
        }));

        player.sendResourcePacks(request);
    }
}
