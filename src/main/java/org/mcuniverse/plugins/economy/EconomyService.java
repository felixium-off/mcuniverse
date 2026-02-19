package org.mcuniverse.plugins.economy;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EconomyService {

    private final EconomyStrategy strategy;

    // 생성자 주입 (Dependency Injection)
    public EconomyService(EconomyStrategy strategy) {
        this.strategy = strategy;
    }

    public CompletableFuture<Void> createAccount(UUID uuid, String name) {
        // 비동기 흐름 제어: 계정이 있는지 확인 후 -> 없으면 생성
        return strategy.hasAccount(uuid).thenCompose(exists -> {
            if (!exists) {
                return strategy.createAccount(uuid, name, 0L);
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    public CompletableFuture<Long> getAccount(UUID uuid, Currency currency) {
        return strategy.getAccount(uuid, currency);
    }

    public CompletableFuture<Boolean> deposit(UUID uuid, Currency currency, long amount) {
        return strategy.deposit(uuid, currency, amount);
    }

    public CompletableFuture<Boolean> withdraw(UUID uuid, Currency currency, long amount) {
        return strategy.withdraw(uuid, currency, amount);
    }

    public CompletableFuture<Void> setAccount(UUID uuid, Currency currency, long amount) {
        return strategy.setAccount(uuid, currency, amount);
    }

    public void deleteAccount(UUID uuid) {
        strategy.deleteAccount(uuid);
    }

    public void expireAccountCache(UUID uuid, long seconds) {
        strategy.expireAccountCache(uuid, seconds);
    }
}