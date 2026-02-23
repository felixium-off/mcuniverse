package org.mcuniverse.systems.entity.model;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import org.mcuniverse.systems.entity.data.MobDTO;

public abstract class BaseMob extends EntityCreature {

    protected final MobDTO mob;

    public BaseMob(MobDTO mob) {
        super(EntityType.fromKey("minecraft:" + mob.getMonsterType().toLowerCase()));
        this.mob = mob;

        applyDisplay();
        applyStats();
    }

    private void applyDisplay() {
        if (mob.getDisplayName() != null) {
            setCustomName(Component.text(mob.getDisplayName()));
            setCustomNameVisible(true);
        }
    }

    private void applyStats() {
        if (mob.getStats() != null) {
            getAttribute(Attribute.MAX_HEALTH).setBaseValue(mob.getStats().getHp());
            setHealth((float) mob.getStats().getHp());

            getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(mob.getStats().getMovementSpeed());
            getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(mob.getStats().getAttackDamage());
        }
    }

    public MobDTO getMob() {
        return mob;
    }
}
