package org.mcuniverse.plugins.essentials;

import net.minestom.server.MinecraftServer;
import org.mcuniverse.plugins.common.GameFeature;
import org.mcuniverse.plugins.essentials.commands.GamemodeCommand;
import org.mcuniverse.plugins.essentials.commands.UtilityCommands;
import org.mcuniverse.plugins.essentials.warp.WarpService;
import org.mcuniverse.plugins.essentials.warp.commands.WarpCommand;
import org.mcuniverse.plugins.essentials.warp.impl.InMemoryWarpStrategy;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class EssentialsFeature implements GameFeature {

    private WarpService warpService;

    @Override
    public void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp) {
        // 1. 워프 서비스 초기화
        this.warpService = new WarpService(new InMemoryWarpStrategy());

        // 2. 명령어 등록
        lamp.register(new GamemodeCommand());
        lamp.register(new UtilityCommands());
        lamp.register(new WarpCommand(warpService));
    }

    @Override
    public void disable(MinecraftServer server) {
        // 데이터 저장 로직이 있다면 여기서 처리
    }
}