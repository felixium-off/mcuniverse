package org.mcuniverse.systems.entity.ai.behavior.impl;

import java.time.Duration;

import org.mcuniverse.systems.entity.ai.behavior.EntityBehavior;
import org.mcuniverse.systems.entity.model.BaseMob;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;

public class MeleeChaseAI implements EntityBehavior {

    @Override
    public String getId() {
        return "melee_chase";
    }

    @Override
    public void init(BaseMob mob) {
        mob.addAIGroup(
                new EntityAIGroupBuilder()
                        .addGoalSelector(new MeleeAttackGoal(mob, 1.6, Duration.ofMillis(500)))
                        .addTargetSelector(new ClosestEntityTarget(mob, 16, e -> e instanceof Player))
                        .build());
    }

    @Override
    public void dispose(BaseMob mob) {
        // Minetom이 entity.remove() 시 자동 정리
    }

}
