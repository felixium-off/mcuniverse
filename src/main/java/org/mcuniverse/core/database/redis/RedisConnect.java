package org.mcuniverse.core.database.redis;

import io.github.cdimascio.dotenv.Dotenv;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 연결을 관리하는 싱글턴 클래스 (Lettuce 기반).
 *
 * <p>
 * 서버 시작 시 {@link #connect()}를, 종료 시 {@link #disconnect()}를 호출하세요.
 *
 * <pre>{@code
 * // 비동기 (게임 서버 메인 스레드 블로킹 방지 권장)
 * RedisConnect.getInstance().async().set("key", "value");
 *
 * // 동기 (짧은 작업에만 사용)
 * RedisConnect.getInstance().sync().get("key");
 * }</pre>
 */
@Slf4j
public class RedisConnect {

    private static final RedisConnect INSTANCE = new RedisConnect();

    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;

    private RedisConnect() {
    }

    public static RedisConnect getInstance() {
        return INSTANCE;
    }

    /* .env의 REDIS_URI와 REDIS_NAME을 사용하여 Redis에 연결합니다. */
    public synchronized void connect() {
        if (client != null)
            throw new IllegalStateException("[REDIS] 이미 연결되어 있습니다!");

        Dotenv env = Dotenv.load();
        String redisUri = env.get("REDIS_URI");

        RedisURI uri = RedisURI.create(redisUri);

        client = RedisClient.create(uri);
        connection = client.connect();

        log.info("\n[REDIS] database connected.\n");
    }

    public synchronized void disconnect() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
        if (client != null) {
            client.shutdown();
            client = null;
        }

        log.info("[REDIS] disconnected.");
    }

    /// 비동기 커맨드를 반환합니다. 게임 서버에서는 이걸 우선 사용하세요.
    ///
    /// # Safety
    /// 주의하세요! 이 메소드는 비동기 [RedisAsyncCommands] 인터페이스를
    /// 직접 전달합니다. 이 경우, 호출자는 `try-with-resources` 블럭을 사용하여
    /// 이 자원을 처리해얗 합니다.
    ///
    /// @throws IllegalStateException connect() 호출 전 접근 시
    public RedisAsyncCommands<String, String> async() {
        return requireConnection().async();
    }

    /// 동기 커맨드를 반환합니다. 메인 스레드 블로킹 주의.
    ///
    /// # Safety
    /// 주의하세요! 이 메소드는 비동기 [RedisAsyncCommands] 인터페이스를
    /// 직접 전달합니다. 이 경우, 호출자는 `try-with-resources` 블럭을 사용하여
    /// 이 자원을 처리해얗 합니다.
    ///
    /// @throws IllegalStateException connect() 호출 전 접근 시
    public RedisCommands<String, String> sync() {
        return requireConnection().sync();
    }

    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    private StatefulRedisConnection<String, String> requireConnection() {
        if (!isConnected())
            throw new IllegalStateException("[REDIS] connect() 먼저 호출하세요.");
        return connection;
    }
}
