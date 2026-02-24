package org.mcuniverse.systems.entity.combat;

import java.util.Random;

public class DamageFormula {

    private static final Random random = new Random();
    private static final float MIN_DAMAGE = 1.0f;

    // 유틸 클래스 인스턴스 X
    private DamageFormula() {
    }

    public static float calculateDamage(CombatStats attacker, CombatStats defender) {
        // 물리 방어
        float effectiveDefense = Math.max(0, defender.getDefense() - attacker.getDefense());
        float physicalDamage = Math.max(0, attacker.getAttackDamage() - effectiveDefense);

        float finalDamage = physicalDamage;
        // 치명타 판정
        boolean isCrit = random.nextFloat() < attacker.getCritRate();
        if (isCrit) {
            finalDamage *= attacker.getCritMultiplier();
        }

        return Math.max(MIN_DAMAGE, finalDamage);
    }

}
