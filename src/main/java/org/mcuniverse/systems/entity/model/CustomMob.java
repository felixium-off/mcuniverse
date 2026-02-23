package org.mcuniverse.systems.entity.model;

import org.mcuniverse.systems.entity.data.MobDTO;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;

public class CustomMob extends EntityCreature {

    private final MobDTO mob;

    public CustomMob(MobDTO mob) {
        super(EntityType.fromKey("minecraft:" + mob.getMonsterType().toLowerCase()));
        this.mob = mob;

        applyDisplay();
        applyStats();
    }

    private void applyDisplay() {
        setCustomName(Component.text(mob.getDisplayName()));
        setCustomNameVisible(true);
    }

    private void applyStats() {
        getAttribute(Attribute.MAX_HEALTH).setBaseValue(mob.getStats().getHp());
        setHealth((float) mob.getStats().getHp());

        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(mob.getStats().getMovementSpeed());
        getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(mob.getStats().getAttackDamage());
    }

    private void applyEquip() {
        if (mob.getEquipment() != null) {

        }
    }

    private void applyAI() {
        if (mob.getAiType() != null) {

        }
    }
}
