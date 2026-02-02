package org.mcuniverse.economy;

import java.util.UUID;

/**
 * 경제 시스템의 핵심 로직을 정의하는 전략 인터페이스입니다.
 * 데이터 저장 방식(Memory, DB 등)에 따라 구현체가 달라집니다.
 */
public interface EconomyStrategy {
    boolean hasAccount(UUID uuid);

    void createAccount(UUID uuid, long initialAmount);

    long getAccount(UUID uuid, EconomyAccount fieldName);

    boolean deposit(UUID uuid, EconomyAccount fieldName, long amount);

    boolean withdraw(UUID uuid, EconomyAccount fieldName, long amount);

    void setAccount(UUID uuid, EconomyAccount fieldName, long amount);
}
