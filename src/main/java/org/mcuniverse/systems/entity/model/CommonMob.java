package org.mcuniverse.systems.entity.model;

import org.mcuniverse.systems.entity.data.MobDTO;

public class CommonMob extends BaseMob {
    public CommonMob(MobDTO mob) {
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

        }
    }
}
