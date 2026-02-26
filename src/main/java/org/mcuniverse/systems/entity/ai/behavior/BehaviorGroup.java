package org.mcuniverse.systems.entity.ai.behavior;

import java.util.List;

import org.mcuniverse.systems.entity.model.BaseMob;

public class BehaviorGroup {

    private final List<EntityBehavior> behaviors;

    public BehaviorGroup(List<EntityBehavior> behaviors) {
        this.behaviors = behaviors;
    }

    public void attach(BaseMob mob) {
        for (EntityBehavior behavior : behaviors) {
            behavior.init(mob);
        }
    }

    public void detach(BaseMob mob) {
        for (EntityBehavior behavior : behaviors) {
            behavior.dispose(mob);
        }
    }
}
