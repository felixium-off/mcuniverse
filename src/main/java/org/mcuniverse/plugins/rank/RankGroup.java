package org.mcuniverse.plugins.rank;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 랭크 시스템의 핵심 인터페이스입니다.
 * 기존 Enum 방식에서 확장 가능한 Interface + Record 패턴으로 변경되었습니다.
 */
public interface RankGroup {

    // --- Registry (Lookup) ---
    Map<String, RankGroup> REGISTRY = new ConcurrentHashMap<>();

    static RankGroup create(String name, String displayName, int level) {
        RankGroup rank = new RankImpl(name, displayName, level);
        REGISTRY.put(name, rank);
        return rank;
    }

    static RankGroup valueOf(String name) {
        RankGroup rank = REGISTRY.get(name);
        if (rank == null) {
            throw new IllegalArgumentException("Unknown rank: " + name);
        }
        return rank;
    }

    static Collection<RankGroup> values() {
        return REGISTRY.values();
    }

    // --- Default Ranks ---
    RankGroup NEWBIE = create("NEWBIE", "뉴비", 1);
    RankGroup MEMBER = create("MEMBER", "멤버", 2);
    RankGroup VIP = create("VIP", "VIP", 3);
    RankGroup ADMIN = create("ADMIN", "관리자", 99);

    // --- Interface Methods ---
    String getName();
    String getDisplayName();
    int getLevel();

    /**
     * 기본 구현체 (Record)
     */
    record RankImpl(String name, String displayName, int level) implements RankGroup {
        @Override public String getName() { return name; }
        @Override public String getDisplayName() { return displayName; }
        @Override public int getLevel() { return level; }
    }
}