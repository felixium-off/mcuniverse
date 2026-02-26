package org.mcuniverse.systems.entity.model;

import org.mcuniverse.systems.entity.ai.behavior.registry.BehaviorRegistry;
import org.mcuniverse.systems.entity.data.MobDTO;

public class CustomMob extends BaseMob {
    public CustomMob(MobDTO mob) {
        super(mob);

        applyEquip();
        applySimpleAI();
    }

    private void applyEquip() {
        if (getMob().getEquipment() != null) {

        }
    }

    private void applySimpleAI() {
        if (getMob().getAiType() != null) {
            BehaviorRegistry.resolve(getMob().getAiType()).attach(this);
        }
    }
}
