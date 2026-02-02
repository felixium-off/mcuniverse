package org.mcuniverse.economy;

import org.mcuniverse.economy.impl.MongoEconomyStrategy;

public class EconomyFactory {

    public enum StorageType {
        JSON,
        MONGODB // MongoDB 타입 추가
    }

    public static EconomyStrategy createStrategy(StorageType type) {
        switch (type) {
            case MONGODB:
                // DatabaseManager를 통해 연결된 인스턴스를 사용하는 전략 반환
                return new MongoEconomyStrategy();
            default:
                throw new IllegalArgumentException("Unknown storage type: " + type);
        }
    }
}