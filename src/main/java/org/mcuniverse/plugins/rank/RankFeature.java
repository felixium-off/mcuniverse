package org.mcuniverse.plugins.rank;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.mcuniverse.plugins.common.GameFeature;
import org.mcuniverse.plugins.common.data.PlayerDataHandler;
import org.mcuniverse.plugins.common.listener.CommonConnectionListener;
import org.mcuniverse.plugins.rank.commands.RankCommand;
import org.mcuniverse.plugins.rank.impl.MongoRankStrategy;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class RankFeature implements GameFeature {

    private final RankService rankService;

    public RankFeature() {
        RankStrategy strategy = new MongoRankStrategy();
        this.rankService = new RankService(strategy);
    }

    @Override
    public void enable(MinecraftServer server, Lamp<MinestomCommandActor> lamp) {
        CommonConnectionListener commonListener = new CommonConnectionListener(server.getGlobalEventHandler());
        commonListener.addHandler(new PlayerDataHandler() {
            @Override
            public void onLoad(Player player) {
                rankService.createRank(player.getUuid(), player.getUsername());
            }

            @Override
            public void onUnload(Player player) {
                rankService.expireRankCache(player.getUuid(), 3600);
            }
        });

        if (lamp != null) {
            lamp.register(new RankCommand(rankService));
        }
    }

    @Override
    public void disable(MinecraftServer server) {}
    
    public RankService getRankService() {
        return rankService;
    }
}