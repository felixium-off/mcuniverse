package org.mcuniverse.core.database.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB 연결을 관리하는 싱글턴 클래스
 * <br/>
 * 서버 시작 시 {@link #connect()}를, 종료 시 {@link #disconnect()}를 호출하세요.
 *
 * <pre>{@code MongoDatabase db = MongoConnect.getInstance().getDatabase(); }</pre>
 */
@Slf4j
public class MongoConnect {

    private static final MongoConnect INSTANCE = new MongoConnect();

    private MongoClient client;
    private MongoDatabase database;

    private MongoConnect() {
    }

    public static MongoConnect getInstance() {
        return INSTANCE;
    }

    /* .env의 MONGODB_URI와 MONGODB_NAME을 사용하여 MongoDB에 연결합니다. */
    public synchronized void connect() {
        if (client != null)
            throw new IllegalStateException("[MONGODB] 이미 연결되어 있습니다!");

        Dotenv env = Dotenv.load(); // Q. T. Felix NOTE: dotenv 의존성은 제거하는 편이 좋아 봉비니다. System.getenv()로 해도 될 듯해용

        String uri = env.get("MONGODB_URI");
        String dbName = env.get("MONGODB_NAME");

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(uri))
            .applyToConnectionPoolSettings(builder -> builder
                    .maxSize(20)
                    .minSize(2))
            .applyToSocketSettings(builder -> builder
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS))
            .build();

        
        client = MongoClients.create(settings);
        database = client.getDatabase(dbName);

        log.info("\n[MONGODB] database connected. -> {}\n", dbName);
    }

    public synchronized void disconnect() {
        if (client == null)
            return;
        client.close();
        client = null;
        database = null;
        log.info("[MONGODB] 연결이 해제 되었습니다.");
    }

    /**
     * 현재 DB를 반환합니다.
     *
     * @throws IllegalStateException connect() 호출 전 접근 시
     */
    public MongoDatabase getDatabase() {
        if (database == null)
            throw new IllegalStateException("[MONGODB] coneect() 먼저 호출하세요.");
        return database;
    }

    public boolean isConnected() {
        return client != null;
    }
}
