package org.mcuniverse.rank;

import java.util.UUID;

public class RankService {

    private final RankStrategy strategy;

    public RankService(RankStrategy strategy) {
        this.strategy = strategy;
    }

    public void createRank(UUID playerUuid, String name) {
        if (!strategy.hasRank(playerUuid)) {
            strategy.createRank(playerUuid, name, Rank.NEWBIE);
        }
    }

    public void expireRankCache(UUID uuid, long seconds) {
        strategy.expireRankCache(uuid, seconds);
    }

    public Rank getRank(UUID playerUuid) {
        return strategy.getRank(playerUuid);
    }

    public void setRank(UUID playerUuid, Rank rank) {
        strategy.setRank(playerUuid, rank);
    }
}
