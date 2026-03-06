package org.mcuniverse.systems.entity.model;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Random;

import org.mcuniverse.systems.entity.data.MobDTO;

public abstract class BaseMob extends EntityCreature {

    protected final MobDTO mob;

    public BaseMob(MobDTO mob) {
        super(EntityType.fromKey("minecraft:" + mob.getMonsterType().toLowerCase()));
        this.mob = mob;

        applyDisplay();
        applyStats();
        applyDeathDrop();
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

    private void applyDeathDrop() {
        eventNode().addListener(EntityDeathEvent.class, event -> {
            if (getInstance() == null)
                return;

            MobDTO.MobRewards rewards = mob.getRewards();
            if (rewards == null || rewards.getDrops() == null)
                return;

            Random random = new Random();
            for (MobDTO.MobRewards.MobDrops drop : rewards.getDrops()) {
                if (random.nextDouble() > drop.getChance()) {
                    continue;
                }

                Material material = Material.fromKey("minecraft:" + drop.getItem().toLowerCase());
                if (material == null) {
                    continue;
                }

                int amount = drop.getMin() + random.nextInt(drop.getMax() - drop.getMin() + 1);
                ItemStack item = ItemStack.of(material, amount);
                ItemEntity itemEntity = new ItemEntity(item);
                itemEntity.setInstance(getInstance(), getPosition().add(0, 1, 0));
            }

        });

    }

    public MobDTO getMob() {
        return mob;
    }
}
