package org.mcuniverse.systems.entity.ai.behavior.impl;

import org.mcuniverse.systems.entity.ai.behavior.EntityBehavior;
import org.mcuniverse.systems.entity.model.BaseMob;

public class IdleBehavior implements EntityBehavior {

    @Override
    public String getId() {
        return "idle";
    }

    @Override
    public void init(BaseMob mob) {

    }

    @Override
    public void dispose(BaseMob mob) {

    }

}
