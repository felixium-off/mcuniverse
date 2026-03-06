package org.mcuniverse.systems.entity.ai.behavior.impl;

import java.time.Duration;
import java.util.Map;

import org.mcuniverse.systems.entity.ai.behavior.EntityBehavior;
import org.mcuniverse.systems.entity.ai.behavior.goal.ItemHoldingFollowGoal;
import org.mcuniverse.systems.entity.ai.behavior.target.ItemHoldingTarget;
import org.mcuniverse.systems.entity.model.BaseMob;

import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.item.Material;

public class PassiveFollowAI implements EntityBehavior {

    private final String followItem;
    private final double followRadius;

    public PassiveFollowAI(Map<String, Object> options) {
        this.followItem = (String) options.getOrDefault("follow_item", "paper");
        this.followRadius = (double) options.getOrDefault("follow_radius", 10.0);
    }

    @Override
    public String getId() {
        return "passive_follow";
    }

    @Override
    public void init(BaseMob mob) {
        Material target = Material.fromKey("minecraft:" + followItem.toLowerCase());
        mob.addAIGroup(new EntityAIGroupBuilder()
                .addGoalSelector(new ItemHoldingFollowGoal(mob, target, Duration.ofMillis(500)))
                .addTargetSelector(new ItemHoldingTarget(mob, followRadius, target))
                .build());
    }

    @Override
    public void dispose(BaseMob mob) {
        // Minestom이 entity.remove() 시 AI 그룹을 자동 정리
    }

}
