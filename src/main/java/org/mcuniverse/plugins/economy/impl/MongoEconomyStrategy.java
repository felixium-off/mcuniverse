package org.mcuniverse.plugins.economy.impl;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.lettuce.core.ScriptOutputType;
import org.bson.Document;
import org.mcuniverse.plugins.common.database.AbstractMongoRedisStrategy;
import org.mcuniverse.plugins.economy.Currency;
import org.mcuniverse.plugins.economy.EconomyStrategy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoEconomyStrategy extends AbstractMongoRedisStrategy implements EconomyStrategy {

    public MongoEconomyStrategy() {
        super("economy", "mc:eco:acct:");
    }

    /**
     * DB에서 economy account 정보를 모두 불러와 Redis Hash에 저장합니다. (Async)
     * 메인 스레드를 차단하지 않기 위해 CompletableFuture를 반환합니다.
     */
    private CompletableFuture<Map<String, String>> loadAndCacheFromDb(UUID uuid) {
        return supplyAsync(() -> {
            Document doc = collection.find(Filters.eq("uuid", uuid.toString())).first();
            if (doc != null) {
                Map<String, String> data = new HashMap<>();
                // 1. 필요한 필드들을 다 가져와서 Map에 담습니다.
                data.put(Currency.BALANCE.getKey(), String.valueOf(doc.get(Currency.BALANCE.getKey(), 0L)));
                data.put(Currency.CASH.getKey(), String.valueOf(doc.get(Currency.CASH.getKey(), 0L)));

                // 2. Redis Hash에 전체 저장
                getRedis().hset(getKey(uuid), data);
                return data;
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> createAccount(UUID uuid, String name, long initialAmount) {
        String key = getKey(uuid);

        if (getRedis().exists(key) == 0) {
            if (collection.countDocuments(Filters.eq("uuid", uuid.toString())) == 0) {
                // Redis Hash 초기 데이터
                Map<String, String> initialData = new HashMap<>();
                initialData.put(Currency.BALANCE.getKey(), String.valueOf(initialAmount));
                initialData.put(Currency.CASH.getKey(), String.valueOf(initialAmount));
                // Redis에는 핵심 경제 데이터만 캐싱 (메모리 절약) - 필요 시 name도 캐싱 가능하나 일단 제외

                getRedis().hset(key, initialData);

                // DB 비동기 저장
                runAsync(() -> {
                    Date now = new Date();
                    Document doc = new Document("uuid", uuid.toString())
                            .append("name", name)
                            .append("balance", initialAmount)
                            .append("cash", initialAmount)
                            .append("created_at", now)
                            .append("updated_at", now);
                    collection.insertOne(doc);
                })
                .exceptionally(e -> {
                    logger.error("[EconomyDB] 저장 실패", e); // DB 저장 실패 시 로그 출력 필수
                    return null;
                });
            } else {
                runAsync(() -> {
                     // [Fix] 유저가 돌아왔으므로 즉시 만료 시간 제거 (Time Bomb 방지)
                     getRedis().persist(key);

                     collection.updateOne(Filters.eq("uuid", uuid.toString()), Updates.set("name", name));
                });

                // 비동기 로드 시작 (결과를 기다릴 필요는 없음)
                loadAndCacheFromDb(uuid);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Long> getAccount(UUID uuid, Currency currency) {
        return getField(uuid, currency);
    }

    @Override
    public CompletableFuture<Boolean> deposit(UUID uuid, Currency currency, long amount) {
        return modifyField(uuid, currency, amount);
    }

    @Override
    public CompletableFuture<Boolean> withdraw(UUID uuid, Currency currency, long amount) {
        return modifyField(uuid, currency, -amount);
    }

    @Override
    public CompletableFuture<Void> setAccount(UUID uuid, Currency currency, long amount) {
        return setField(uuid, currency.getKey(), amount);
    }

    /**
     * 특정 필드 값 가져오기 (Redis -> DB fallback Async)
     */
    private CompletableFuture<Long> getField(UUID uuid, Currency currency) {
        String key = getKey(uuid);
        String cached = getRedis().hget(key, currency.getKey());

        if (cached != null) {
            getRedis().persist(key);
            return CompletableFuture.completedFuture(Long.parseLong(cached));
        }

        // Cache Miss: 비동기로 DB에서 로드 후 반환
        return loadAndCacheFromDb(uuid).thenApply(data -> 
            data != null ? Long.parseLong(data.getOrDefault(currency.getKey(), "0")) : 0L
        );
    }

    /**
     * 특정 필드 값 수정하기 (증가/감소) + Lua Script로 원자성(Atomicity) 보장
     * 이제 CompletableFuture를 반환하여 메인 스레드를 차단하지 않습니다.
     */
    private CompletableFuture<Boolean> modifyField(UUID uuid, Currency currency, long amount) {
        String key = getKey(uuid);
        String field = currency.getKey();

        // 1. 데이터 존재 여부 확인 (비동기 체이닝)
        CompletableFuture<Boolean> ensureLoaded = (getRedis().exists(key) > 0) 
                ? CompletableFuture.completedFuture(true) 
                : loadAndCacheFromDb(uuid).thenApply(data -> data != null);

        return ensureLoaded.thenApplyAsync(loaded -> {
            if (!loaded) return false;

            // 2. Lua Script 작성
            String script = """
                    local current = redis.call('HGET', KEYS[1], ARGV[1])
                    if not current then current = 0 end
                
                    local val = tonumber(current)
                    local change = tonumber(ARGV[2])
                
                    -- 잔액 부족 확인
                    if val + change < 0 then return 0 end
                
                    -- 값 변경
                    redis.call('HINCRBY', KEYS[1], ARGV[1], ARGV[2])
                    
                    -- [추가된 부분] 만료 시간 제거 (PERSIST)
                    -- 돈이 바뀌었다는 건 유저가 살아있다는 뜻이므로 즉시 살려냅니다.
                    redis.call('PERSIST', KEYS[1])
                    
                    return 1
                """;

            // 3. 스크립트 실행 (Redis는 싱글 스레드이므로 매우 빠름)
            boolean success = getRedis().eval(
                    script,
                    ScriptOutputType.BOOLEAN,
                    new String[]{key}, // KEYS[1]
                    field,             // ARGV[1]
                    String.valueOf(amount) // ARGV[2]
            );

            if (!success) {
                return false; // 잔액 부족으로 거절됨
            }

            // 4. Redis 성공 시에만 DB 비동기 업데이트 진행 (Write-Behind)
            // 이미 별도 스레드(thenApplyAsync)일 수 있으나, DB 작업은 확실히 DB Executor로 보냄
            runAsync(() -> {
            if (amount < 0) {
                // 출금 시 DB 안전장치 (2중 방어)
                collection.updateOne(
                        Filters.and(Filters.eq("uuid", uuid.toString()), Filters.gte(field, -amount)),
                        Updates.combine(
                            Updates.inc(field, amount),
                            Updates.set("updated_at", new Date())
                        )
                );
            } else {
                // 입금
                collection.updateOne(
                        Filters.eq("uuid", uuid.toString()),
                        Updates.combine(
                            Updates.inc(field, amount),
                            Updates.set("updated_at", new Date())
                        )
                );
            }
        });

            return true;
        }, getExecutor());
    }

    /**
     * 특정 필드 값 덮어쓰기
     */
    private CompletableFuture<Void> setField(UUID uuid, String type, long value) {
        String key = getKey(uuid);
        
        return runAsync(() -> {
            getRedis().hset(key, type, String.valueOf(value));
            
            // DB 업데이트
            getExecutor().execute(() -> {
                collection.updateOne(
                        Filters.eq("uuid", uuid.toString()),
                        Updates.combine(
                            Updates.set(type, value),
                            Updates.set("updated_at", new Date())
                        )
                );
            });
        });
    }

    @Override
    public CompletableFuture<Boolean> hasAccount(UUID uuid) {
        String key = getKey(uuid);
        if (getRedis().exists(key) > 0) return CompletableFuture.completedFuture(true);
        return loadAndCacheFromDb(uuid).thenApply(data -> data != null);
    }

    @Override
    public CompletableFuture<Void> deleteAccount(UUID uuid) {
        String key = getKey(uuid);

        getRedis().del(key);
        return runAsync(() -> {
                    collection.deleteOne(Filters.eq("uuid", uuid.toString()));
                })
                .exceptionally(e -> {
                    logger.error("[EconomyDB] 데이터 삭제 오류", e);
                    return null;
                });
    }

    @Override
    public void expireAccountCache(UUID uuid) {
        expireAccountCache(uuid, 3600);
    }

    @Override
    public void expireAccountCache(UUID uuid, long seconds) {
        expireCache(uuid, seconds);
    }
}