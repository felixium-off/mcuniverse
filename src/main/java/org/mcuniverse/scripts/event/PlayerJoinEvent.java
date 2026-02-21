package org.mcuniverse.scripts.event;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import org.mcuniverse.systems.world.WorldService;

import net.minestom.server.event.GlobalEventHandler;
import org.mcuniverse.systems.resourcepack.ResourcepackService;

public class PlayerJoinEvent {

    private WorldService worldService;
    private ResourcepackService resourcepackService;

    public PlayerJoinEvent(WorldService worldService, ResourcepackService resourcepackService) {
        this.worldService = worldService;
        this.resourcepackService = resourcepackService;
    }

    public void register(GlobalEventHandler globalEventHandler) {
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            InstanceContainer instanceContainer = worldService.createWorld("lobby");
            event.setSpawningInstance(instanceContainer);

            Player player = event.getPlayer();

            player.sendResourcePacks(resourcepackService.getRequest());
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();
            player.sendMessage("안녕하세요!");
        });
    }
}
