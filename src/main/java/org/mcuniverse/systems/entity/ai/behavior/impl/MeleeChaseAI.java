package org.mcuniverse.systems.entity.ai.behavior.impl;

import java.time.Duration;
import java.util.Map;

import org.mcuniverse.systems.entity.ai.behavior.EntityBehavior;
import org.mcuniverse.systems.entity.model.BaseMob;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;

public class MeleeChaseAI implements EntityBehavior {

    private final double attackRange;
    private final long attackDelay;
    private final int strollRadius;

    public MeleeChaseAI(Map<String, Object> options) {
        Object rawRange = options.getOrDefault("attack_range", 1.6);
        this.attackRange = (rawRange instanceof Number n) ? n.doubleValue() : 1.6;

        Object rawDelay = options.getOrDefault("attack_delay", 500);
        this.attackDelay = rawDelay instanceof Number ? ((Number) rawDelay).longValue() : 500;

        Object rawRadius = options.getOrDefault("stroll_radius", 10);
        this.strollRadius = (rawRadius instanceof Number n) ? n.intValue() : 10;
    }

    @Override
    public String getId() {
        return "melee_chase";
    }

    @Override
    public void init(BaseMob mob) {
        mob.addAIGroup(
                new EntityAIGroupBuilder()
                        .addTargetSelector(new ClosestEntityTarget(mob, 16.0, entity -> entity instanceof Player))
                        .addGoalSelector(new MeleeAttackGoal(mob, attackRange, Duration.ofMillis(attackDelay)))
                        .addGoalSelector(new RandomStrollGoal(mob, strollRadius))
                        .build());
    }

    @Override
    public void dispose(BaseMob mob) {
        // Minetom이 entity.remove() 시 자동 정리
    }

}
