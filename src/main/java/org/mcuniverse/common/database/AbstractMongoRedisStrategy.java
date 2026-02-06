package org.mcuniverse.common.database;

import com.mongodb.client.MongoCollection;
import io.lettuce.core.api.sync.RedisCommands;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * MongoDB와 Redis를 함께 사용하는 전략 패턴의 공통 부모 클래스입니다.
 * 인프라(DB 연결, 스레드 관리, 키 생성) 로직을 캡슐화합니다.
 */
public abstract class AbstractMongoRedisStrategy {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final MongoCollection<Document> collection;
    private final String keyPrefix;

    protected AbstractMongoRedisStrategy(String collectionName, String keyPrefix) {
        this.collection = DatabaseManager.getInstance().getMongoDatabase().getCollection(collectionName);
        this.keyPrefix = keyPrefix;
    }

    /**
     * Redis 연결을 지연 로딩(Lazy Loading)으로 가져옵니다.
     * 생성자 시점의 NPE 방지 및 연결 안정성을 보장합니다.
     */
    protected RedisCommands<String, String> getRedis() {
        return DatabaseManager.getInstance().getRedisSync();
    }

    protected String getKey(UUID uuid) {
        return keyPrefix + uuid.toString();
    }

    /**
     * DB 작업을 처리할 전용 스레드 풀을 반환합니다.
     */
    protected ExecutorService getExecutor() {
        return DatabaseManager.getInstance().getDbExecutor();
    }

    /**
     * 비동기 작업을 쉽게 실행하기 위한 헬퍼 메서드 (Runnable)
     */
    protected CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, getExecutor());
    }

    /**
     * 비동기 작업을 쉽게 실행하기 위한 헬퍼 메서드 (Supplier)
     */
    protected <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, getExecutor());
    }

    public void expireCache(UUID uuid, long seconds) {
        getRedis().expire(getKey(uuid), seconds);
    }
}