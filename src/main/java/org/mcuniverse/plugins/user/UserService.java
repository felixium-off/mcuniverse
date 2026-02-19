package org.mcuniverse.plugins.user;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserService {

    private final UserStrategy strategy;

    public UserService(UserStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * 유저 데이터를 로드하거나, 없으면 새로 생성합니다.
     * 접속 시 IP 업데이트 로직이 포함됩니다.
     */
    public CompletableFuture<User> loadOrCreateUser(UUID uuid, String username, String ip) {
        return strategy.getUser(uuid).thenCompose(user -> {
            if (user == null) {
                // 신규 유저 생성
                return strategy.createUser(uuid, username, ip);
            } else {
                // 기존 유저: IP 및 마지막 접속일 업데이트
                user.setLastUpdateIp(ip);
                user.setLastUpdated(new Date());
                // 비동기 저장 (결과 기다리지 않음)
                strategy.saveUser(user);
                return CompletableFuture.completedFuture(user);
            }
        });
    }

    public CompletableFuture<Void> saveUser(User user) {
        return strategy.saveUser(user);
    }

    public CompletableFuture<Void> lockedUser(UUID uuid, boolean locked) {
        return strategy.getUser(uuid).thenCompose(user -> {
            if (user == null) return CompletableFuture.completedFuture(null);
            user.setLockedDate(locked ? new Date() : null);
            return strategy.saveUser(user);
        });
    }
}
