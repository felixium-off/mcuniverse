package org.mcuniverse.plugins.rank.impl;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.mcuniverse.plugins.common.database.AbstractMongoRedisStrategy;
import org.mcuniverse.plugins.rank.RankGroup;
import org.mcuniverse.plugins.rank.RankStrategy;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoRankStrategy extends AbstractMongoRedisStrategy implements RankStrategy {

    private static final String FIELD_RANK = "rank";

    public MongoRankStrategy() {
        super("rank", "mc:rank:acct:");
    }

    /**
     * DB에서 랭크 정보를 불러와 Redis에 캐싱합니다. (Async)
     */
    private CompletableFuture<RankGroup> loadAndCacheFromDb(UUID uuid) {
        return supplyAsync(() -> {
            Document doc = collection.find(Filters.eq("uuid", uuid.toString())).first();
            if (doc != null) {
                String rankName = doc.getString(FIELD_RANK);
                if (rankName != null) {
                    try {
                        RankGroup rank = RankGroup.valueOf(rankName);
                        // Redis 캐싱
                        getRedis().hset(getKey(uuid), FIELD_RANK, rankName);
                        return rank;
                    } catch (IllegalArgumentException e) {
                        logger.warn("[RankDB] 유효하지 않은 랭크 데이터: {}", rankName);
                    }
                }
            }
            return RankGroup.NEWBIE; // 기본값
        });
    }

    @Override
    public CompletableFuture<Void> createRank(UUID uuid, String name) {
        String key = getKey(uuid);

        // 접속 시 로직 변경: Redis 확인보다 DB(Source of Truth)를 우선 확인하여 동기화
        return runAsync(() -> {
            Document doc = collection.find(Filters.eq("uuid", uuid.toString())).first();

            if (doc == null) {
                // 1. DB에 데이터가 없으면 -> 신규 유저 생성
                RankGroup initialRank = RankGroup.NEWBIE;

                // Redis 저장
                getRedis().hset(key, FIELD_RANK, initialRank.getName());
                getRedis().persist(key);

                // DB 저장
                Date now = new Date();
                Document newDoc = new Document("uuid", uuid.toString())
                        .append("name", name)
                        .append(FIELD_RANK, initialRank.getName())
                        .append("created_at", now)
                        .append("updated_at", now)
                        .append("expired_at", now);
                collection.insertOne(newDoc);
                
                logger.info("[Rank] 신규 유저 데이터 생성: {} ({})", name, initialRank);
            } else {
                // 2. DB에 데이터가 있으면 -> Redis 강제 동기화 (수동 DB 수정 반영)
                String dbRank = doc.getString(FIELD_RANK);
                
                // Redis 덮어쓰기
                getRedis().hset(key, FIELD_RANK, dbRank);
                getRedis().persist(key);

                // 닉네임이 변경되었다면 DB 업데이트 (비동기)
                if (!name.equals(doc.getString("name"))) {
                    collection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("name", name));
                }
            }
        });
    }

    @Override
    public CompletableFuture<RankGroup> getRank(UUID uuid) {
        String key = getKey(uuid);
        String cachedRank = getRedis().hget(key, FIELD_RANK);

        if (cachedRank != null) {
            getRedis().persist(key); // Keep-Alive
            try {
                return CompletableFuture.completedFuture(RankGroup.valueOf(cachedRank));
            } catch (IllegalArgumentException e) {
                return CompletableFuture.completedFuture(RankGroup.NEWBIE);
            }
        }

        // Cache Miss -> DB Load
        return loadAndCacheFromDb(uuid);
    }

    @Override
    public CompletableFuture<Void> setRank(UUID uuid, RankGroup rank) {
        String key = getKey(uuid);
        
        return runAsync(() -> {
            // 1. Redis 즉시 반영 & 생존 신고
            getRedis().hset(key, FIELD_RANK, rank.getName());
            getRedis().persist(key);

            // 2. DB 비동기 반영
            getExecutor().execute(() -> {
                collection.updateOne(
                        Filters.eq("uuid", uuid.toString()),
                        Updates.combine(
                                Updates.set(FIELD_RANK, rank.getName()),
                                Updates.set("updated_at", new Date())
                        )
                );
            });
        });
    }

    @Override
    public void expireRankCache(UUID uuid, long seconds) {
        expireCache(uuid, seconds);
    }
}