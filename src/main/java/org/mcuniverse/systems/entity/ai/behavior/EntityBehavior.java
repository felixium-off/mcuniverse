package org.mcuniverse.systems.entity.ai.behavior;

import org.mcuniverse.systems.entity.model.BaseMob;

public interface EntityBehavior {
    String getId(); // "melee_chase", "idle" 등

    void init(BaseMob mob); // 장착 시 1회 — AI 등록

    void dispose(BaseMob mob); // 제거 시 — 정리 작업
}
