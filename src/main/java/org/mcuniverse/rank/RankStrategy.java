package org.mcuniverse.rank;

import java.util.UUID;

/**
 * 등급 시스템의 핵심 로직을 정의하는 전략 인터페이스입니다.
 * 데이터 저장 방식(Memory, DB 등)에 따라 구현체가 달라집니다.
 */
public interface RankStrategy {

    boolean hasRank(UUID uuid);

    void createRank(UUID uuid, Rank rank);

    Rank getRank(UUID uuid);

    void setRank(UUID uuid, Rank rank);

    void onShutdown();
}
