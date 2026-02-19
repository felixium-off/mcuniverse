package org.mcuniverse.plugins.cosmetics.storage;

import java.util.Set;
import java.util.UUID;

public interface CosmeticRepository {
    // 플레이어의 장착 중인 치장품 ID 목록 저장
    void save(UUID uuid, Set<String> equippedIds);

    // 저장된 치장품 ID 목록 불러오기
    Set<String> load(UUID uuid);
}