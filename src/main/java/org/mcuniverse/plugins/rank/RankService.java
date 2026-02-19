package org.mcuniverse.plugins.rank;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RankService {

    private final RankStrategy strategy;

    public RankService(RankStrategy strategy) {
        this.strategy = strategy;
    }

    public CompletableFuture<Void> createRank(UUID uuid, String name) {
        return strategy.createRank(uuid, name);
    }

    public CompletableFuture<RankGroup> getRank(UUID uuid) {
        return strategy.getRank(uuid);
    }

    public CompletableFuture<Void> setRank(UUID uuid, RankGroup rank) {
        return strategy.setRank(uuid, rank);
    }

    public void expireRankCache(UUID uuid, long seconds) {
        strategy.expireRankCache(uuid, seconds);
    }
}