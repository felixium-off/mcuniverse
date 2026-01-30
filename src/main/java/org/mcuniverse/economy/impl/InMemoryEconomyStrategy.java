package org.mcuniverse.economy.impl;

import org.mcuniverse.economy.EconomyStrategy;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 메모리 기반의 고성능 경제 구현체입니다.
 * ConcurrentHashMap을 사용하여 스레드 안전성을 보장합니다.
 */
public class InMemoryEconomyStrategy implements EconomyStrategy {

    private final Map<UUID, BigDecimal> balances = new ConcurrentHashMap<>();

    @Override
    public boolean hasAccount(UUID uuid) {
        return balances.containsKey(uuid);
    }

    @Override
    public void createAccount(UUID uuid, BigDecimal initialBalance) {
        balances.putIfAbsent(uuid, initialBalance);
    }

    @Override
    public BigDecimal getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, BigDecimal.ZERO);
    }

    @Override
    public boolean deposit(UUID uuid, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return false; // 0 이하 입금 방지

        // 원자적 연산: 키가 존재할 때만 값을 업데이트
        BigDecimal result = balances.computeIfPresent(uuid, (key, current) -> current.add(amount));
        return result != null;
    }

    @Override
    public boolean withdraw(UUID uuid, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return false;

        // 원자적 연산: 잔액 확인과 차감을 동시에 수행하여 동시성 문제 해결
        // compute 메서드 내부는 해당 키에 대해 락이 걸린 것처럼 동작함
        try {
            balances.compute(uuid, (key, current) -> {
                if (current == null) throw new IllegalStateException("Account not found");
                if (current.compareTo(amount) < 0) throw new IllegalStateException("Insufficient funds");
                return current.subtract(amount);
            });
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Override
    public void onShutdown() {
        // 메모리 방식은 프로세스 종료 시 OS가 메모리를 회수하므로 별도 작업이 필요 없습니다.
        // 추후 파일 저장 기능이 추가된다면 여기서 saveToFile() 등을 호출합니다.
        balances.clear();
    }
}