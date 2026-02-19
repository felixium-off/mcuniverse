package org.mcuniverse.plugins.economy;

/**
 * 경제 시스템에서 사용하는 화폐의 단위를 정의하는 인터페이스입니다.
 * 기존 Enum 방식의 OCP 위반 문제를 해결하기 위해 도입되었습니다.
 */
public interface Currency {

    /**
     * DB 및 Redis에 저장될 필드 키(Key)입니다.
     */
    String getKey();

    String getDisplayName();

    // 기본 화폐 상수 정의 (편의성)
    Currency BALANCE = new CurrencyImpl("balance", "골드");
    Currency CASH = new CurrencyImpl("cash", "캐시");

    record CurrencyImpl(String key, String displayName) implements Currency {
        @Override
        public String getKey() { return key; }
        @Override
        public String getDisplayName() { return displayName; }
    }
}