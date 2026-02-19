package org.mcuniverse.plugins.user;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import org.mcuniverse.plugins.common.GameFeature;
import org.mcuniverse.plugins.user.commands.UserCommand;
import org.mcuniverse.plugins.user.impl.MongoUserStrategy;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class UserFeature implements GameFeature {

    private UserService userService;

    @Override
    public void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp) {
        // 전략 및 서비스 초기화
        UserStrategy strategy = new MongoUserStrategy();
        this.userService = new UserService(strategy);

        lamp.register(new UserCommand(userService));

        GlobalEventHandler handler = server.getGlobalEventHandler();

        // 1. 접속 (Login)
        handler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            String ip = player.getPlayerConnection().getRemoteAddress().toString();

            userService.loadOrCreateUser(player.getUuid(), player.getUsername(), ip)
                    .thenAccept(user -> {
                        if (user.isLocked()) {
                            System.out.println("User Locked: " + user.getUsername());
                            player.kick("블랙리스트에 포함 되어 있습니다. 자세한 내용은 운영진에게 메세지를 보내주세요.");
                            return;
                        }
                        System.out.println("User Loaded: " + user.getUsername());
                    });
        });
    }

    @Override
    public void disable(MinecraftServer server) {
        // 필요 시 종료 로직 (캐시 정리 등)
    }
}
