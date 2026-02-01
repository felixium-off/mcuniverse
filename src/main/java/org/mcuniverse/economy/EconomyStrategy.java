package org.mcuniverse.economy;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 경제 시스템의 핵심 로직을 정의하는 전략 인터페이스입니다.
 * 데이터 저장 방식(Memory, DB 등)에 따라 구현체가 달라집니다.
 */
public interface EconomyStrategy {

    boolean hasAccount(UUID uuid);

    void createAccount(UUID uuid, BigDecimal initialBalance);

    BigDecimal getBalance(UUID uuid);

    boolean deposit(UUID uuid, BigDecimal amount);

    boolean withdraw(UUID uuid, BigDecimal amount);

    void setBalance(UUID uuid, BigDecimal amount);

    void onShutdown();
}