package org.mcuniverse.systems.entity.ai.behavior.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.FollowTargetGoal;
import net.minestom.server.item.Material;

import java.time.Duration;

import org.mcuniverse.systems.entity.model.BaseMob;

/**
 * Follows a player if they are holding the required item.
 */
public class ItemHoldingFollowGoal extends FollowTargetGoal {

    private final BaseMob mob;
    private final Material requiredItem;

    public ItemHoldingFollowGoal(BaseMob mob, Material requiredItem, Duration delay) {
        super(mob, delay);
        this.mob = mob;
        this.requiredItem = requiredItem;
    }

    @Override
    public boolean shouldStart() {
        Entity target = mob.getTarget();
        if (!(target instanceof Player player))
            return false;
        return requiredItem == null || player.getItemInMainHand().material() == requiredItem;
    }

    @Override
    public boolean shouldEnd() {
        Entity target = mob.getTarget();
        if (!(target instanceof Player player))
            return true;
        return requiredItem == null || player.getItemInMainHand().material() != requiredItem;
    }

    @Override
    public void end() {
        // 타겟을 해제하지 않으면 shouldStart()가 바로 true를 반환해 goal이 즉시 재시작됨
        mob.setTarget(null);
        super.end(); // navigator.setPathTo(null)
    }
}
