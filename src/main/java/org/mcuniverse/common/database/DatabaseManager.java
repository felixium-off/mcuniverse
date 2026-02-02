package org.mcuniverse.common.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.mcuniverse.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * MongoDB 및 Redis 연결을 관리하는 싱글톤 매니저입니다.
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;

    // DB 작업을 처리할 전용 스레드 풀 (직원 4명 고용)
    private final ExecutorService dbExecutor;

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> redisConnection;

    private DatabaseManager() {
        // 1. 스레드 풀 생성 (CPU 코어 수 * 2 권장)
        int cores = Runtime.getRuntime().availableProcessors();
        this.dbExecutor = Executors.newFixedThreadPool(Math.max(4, cores * 2));

        // DB 연결 초기화
        initMongo(ConfigManager.get("mongodb.uri", "mongodb://localhost:27017"), "mcuniverse");
        initRedis(ConfigManager.get("redis.uri", "redis://localhost:6379"));
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initMongo(String connectionString, String dbName) {
        try {
            this.mongoClient = MongoClients.create(connectionString);
            this.mongoDatabase = mongoClient.getDatabase(dbName);
            System.out.println("[Database] MongoDB Connected.");
        } catch (Exception e) {
            logger.error("MongoDB disconneted", e);
        }
    }

    private void initRedis(String uri) {
        try {
            this.redisClient = RedisClient.create(uri);
            this.redisConnection = redisClient.connect();
            System.out.println("[Database] Redis Connected.");
        } catch (Exception e) {
            logger.error("Redis disconneted", e);
        }
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public RedisCommands<String, String> getRedisSync() {
        return redisConnection.sync();
    }

    // 다른 클래스들이 DB 작업을 맡길 때 사용할 실행기(Executor)를 제공
    public ExecutorService getDbExecutor() {
        return dbExecutor;
    }

    public void shutdown() {
        logger.info("🛑 데이터베이스 서비스 종료 시작...");

        // 1. "더 이상 새 작업 받지 마!" (셔터 내림)
        dbExecutor.shutdown();

        try {
            // 2. "이미 들어온 작업은 끝날 때까지 10초만 기다려줄게"
            if (!dbExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("⚠️ 일부 DB 작업이 10초 내에 완료되지 않아 강제 종료합니다.");
                dbExecutor.shutdownNow();
            } else {
                logger.info("✅ 모든 대기 중인 DB 작업이 완료되었습니다.");
            }
        } catch (InterruptedException e) {
            logger.error("❌ 종료 대기 중 인터럽트 발생!", e);
            dbExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 3. 연결 끊기 (이제 안전함)
        if (redisConnection != null) redisConnection.close();
        if (redisClient != null) redisClient.shutdown();
        if (mongoClient != null) mongoClient.close();

        logger.info("👋 데이터베이스 연결이 완전히 종료되었습니다.");
    }

    public static void close() {
        if (instance != null) {
            instance.shutdown();
        }
    }
}