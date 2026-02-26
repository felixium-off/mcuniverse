package org.mcuniverse.systems.entity.combat;

import org.mcuniverse.systems.entity.data.MobDTO;

import lombok.Builder;
import lombok.Value;
import org.mcuniverse.systems.entity.data.PlayerDTO;

/**
 * Entity의 전투 관련 스탯
 * 
 * @param attackDamage   공격력
 * @param critRate       치명타 확률
 * @param critMultiplier 치명타 배율
 * @param defense        방어력
 */
@Value
@Builder
public class CombatStats {

    float attackDamage;
    float critRate; // 0.0 ~ 1.0
    float critMultiplier; // ex) 1.5
    float defense;

    public static CombatStats fromMobStats(MobDTO.MobStats stats) {
        if (stats == null) {
            return CombatStats.builder()
                    .attackDamage(1f)
                    .critMultiplier(1f).build();
        }

        return CombatStats.builder()
                .attackDamage(stats.getAttackDamage())
                .critRate(stats.getCritRate())
                .critMultiplier(stats.getCritMultiplier())
                .defense(stats.getDefense())
                .build();
    }

    public static CombatStats fromPlayerStats(PlayerDTO.PlayerStats stats) {
        if (stats == null) {
            // PlayerDTO 미로드 상태 → 기본값 폴백
            return CombatStats.builder()
                    .attackDamage(5f)
                    .critRate(0.05f)
                    .critMultiplier(1.5f)
                    .defense(0f)
                    .build();
        }

        return CombatStats.builder()
                .attackDamage(stats.getAttackDamage())
                .critRate(stats.getCritRate())
                .critMultiplier(stats.getCritMultiplier())
                .defense(stats.getDefense())
                .build();
    }
}
