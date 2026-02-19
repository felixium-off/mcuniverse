package org.mcuniverse.plugins.economy;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 경제 시스템의 핵심 로직을 정의하는 전략 인터페이스입니다.
 * 데이터 저장 방식(Memory, DB 등)에 따라 구현체가 달라집니다.
 */
public interface EconomyStrategy {
    CompletableFuture<Boolean> hasAccount(UUID uuid);

    CompletableFuture<Void> createAccount(UUID uuid, String name, long initialAmount);

    CompletableFuture<Long> getAccount(UUID uuid, Currency currency);

    CompletableFuture<Boolean> deposit(UUID uuid, Currency currency, long amount);

    CompletableFuture<Boolean> withdraw(UUID uuid, Currency currency, long amount);

    CompletableFuture<Void> setAccount(UUID uuid, Currency currency, long amount);

    CompletableFuture<Void> deleteAccount(UUID uuid);

    void expireAccountCache(UUID uuid);

    default void expireAccountCache(UUID uuid, long seconds) {}
}
