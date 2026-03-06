package org.mcuniverse.core.database.postgre;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

/**
 * PostgreSQL 연결을 관리하는 싱글턴 클래스
 * <br/>
 * 서버 시작 시 {@link #connect()}를, 종료 시 {@link #disconnect()}를 호출하세요.
 *
 * <pre>{@code
 * Connection conn = PostgresConnect.getInstance().getConnection();
 * }</pre>
 */
@Slf4j
public class PostgreConnect {

    private static final PostgreConnect INSTANCE = new PostgreConnect();

    private HikariDataSource dataSource;

    private PostgreConnect() {
    }

    public static PostgreConnect getInstance() {
        return INSTANCE;
    }

    public synchronized void connect() {
        if (dataSource != null)
            throw new IllegalStateException("[POSTGRESQL] 이미 연결되어 있습니다!");

        Dotenv dotenv = Dotenv.load();

        String host = dotenv.get("POSTGRESQL_URI");
        String port = dotenv.get("POSTGRESQL_PORT");
        String db = dotenv.get("POSTGRESQL_DB");
        String user = dotenv.get("POSTGRESQL_USERNAME");
        String password = dotenv.get("POSTGRESQL_PASSWORD");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + db);
        config.setUsername(user);
        config.setPassword(password);
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(1800000);
        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);

        log.info("\n[POSTGRESQL] database connected. -> {}\n", config.getDataSourceClassName());
    }

    public synchronized void disconnect() {
        if (dataSource == null)
            return;
        dataSource.close();
        dataSource = null;
        log.info("[POSTGRESQL] 연결이 해제 되었습니다.");
    }

    /**
     * 현재 DB를 반환합니다.
     *
     * @throws IllegalStateException connect() 호출 전 접근 시
     */
    public java.sql.Connection getConnection() throws SQLException {
        if (dataSource == null)
            throw new IllegalStateException("[POSTGRESQL] connect() 먼저 호출하세요.");
        return dataSource.getConnection();
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }
}
