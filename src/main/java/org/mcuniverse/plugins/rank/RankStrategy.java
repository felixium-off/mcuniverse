package org.mcuniverse.plugins.rank;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RankStrategy {

    CompletableFuture<Void> createRank(UUID uuid, String name);

    CompletableFuture<RankGroup> getRank(UUID uuid);

    CompletableFuture<Void> setRank(UUID uuid, RankGroup rank);

    void expireRankCache(UUID uuid, long seconds);
}