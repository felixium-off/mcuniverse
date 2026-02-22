package org.mcuniverse.plugins.common.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * MongoDB 및 Redis 연결을 관리하는 싱글톤 매니저입니다.
 */
@Slf4j
public class DatabaseManager {

    private static DatabaseManager instance;

    // 다른 클래스들이 DB 작업을 맡길 때 사용할 실행기(Executor)를 제공
    // DB 작업을 처리할 전용 스레드 풀 (직원 4명 고용)
    @Getter
    private final ExecutorService dbExecutor;
    private final Dotenv dotenv; // Q. T. Felix NOTE: 이 env 호출 방식은 보안상 권장되지 않습니다! System.getenv() 메소드를 통해 구현 권장합니다.

    private MongoClient mongoClient;
    @Getter
    private MongoDatabase mongoDatabase;
    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> redisConnection;

    private DatabaseManager() {
        int cores = Runtime.getRuntime().availableProcessors();
        this.dbExecutor = Executors.newFixedThreadPool(Math.max(4, cores * 2));

        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
        String mongoUri = dotenv.get("MONGODB_URI", "mongodb://localhost:27017");
        String mongoDatabase = dotenv.get("MONGODB_DATABASE", "mcuniverse");
        String redisUri = dotenv.get("REDIS_URI", "redis://localhost:6379");

        initMongo(mongoUri, mongoDatabase);
        initRedis(redisUri);
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
            log.error("MongoDB disconneted", e);
        }
    }

    private void initRedis(String uri) {
        try {
            this.redisClient = RedisClient.create(uri);
            this.redisConnection = redisClient.connect();
            System.out.println("[Database] Redis Connected.");
        } catch (Exception e) {
            log.error("Redis disconneted", e);
        }
    }

    public RedisCommands<String, String> getRedisSync() {
        if (redisConnection == null) {
            log.error("❌ Redis 연결이 초기화되지 않았습니다. 요청을 처리할 수 없습니다.");
            throw new IllegalStateException("Redis Connection is not established.");
        }
        return redisConnection.sync();
    }

    public void shutdown() {
        log.info("🛑 데이터베이스 서비스 종료 시작...");

        // 1. "더 이상 새 작업 받지 마!" (셔터 내림)
        dbExecutor.shutdown();

        try {
            // 2. "이미 들어온 작업은 끝날 때까지 10초만 기다려줄게"
            if (!dbExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("⚠️ 일부 DB 작업이 10초 내에 완료되지 않아 강제 종료합니다.");
                dbExecutor.shutdownNow();
            } else {
                log.info("✅ 모든 대기 중인 DB 작업이 완료되었습니다.");
            }
        } catch (InterruptedException e) {
            log.error("❌ 종료 대기 중 인터럽트 발생!", e);
            dbExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 3. 연결 끊기 (이제 안전함)
        if (redisConnection != null) redisConnection.close();
        if (redisClient != null) redisClient.shutdown();
        if (mongoClient != null) mongoClient.close();

        log.info("👋 데이터베이스 연결이 완전히 종료되었습니다.");
    }

    public static void close() {
        if (instance != null) {
            instance.shutdown();
        }
    }
}