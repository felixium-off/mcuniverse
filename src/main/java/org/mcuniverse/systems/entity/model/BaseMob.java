package org.mcuniverse.systems.entity.model;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

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

            var rewards = mob.getRewards();
            if (rewards == null || rewards.getDrops() == null)
                return;
            rewards.getDrops().stream()
                    .filter(this::rollDrop)
                    .map(this::resolveItemStack)
                    .filter(Objects::nonNull)
                    .forEach(this::spawnDropItem);

            giveExp(rewards.getExp());
            giveMoney(rewards.getMoney());
        });

    }

    // 확률 판정
    private boolean rollDrop(MobDTO.MobRewards.MobDrops drop) {
        return ThreadLocalRandom.current().nextDouble() <= drop.getChance();
    }

    // 아이템 변환 담당
    private ItemStack resolveItemStack(MobDTO.MobRewards.MobDrops drop) {
        var material = Material.fromKey("minecraft:" + drop.getItem().toLowerCase());
        if (material == null)
            return null;

        int amount = ThreadLocalRandom.current().nextInt(drop.getMin(), drop.getMax() + 1);
        return ItemStack.of(material, amount);
    }

    // 아이템 생성 담당
    private void spawnDropItem(ItemStack item) {
        var entity = new ItemEntity(item);
        entity.setInstance(getInstance(), getPosition().add(0, 1, 0));
    }

    private void giveExp(MobDTO.MobRewards.MobExp exp) {
        if (exp == null)
            return;

        int amount = ThreadLocalRandom.current().nextInt(exp.getMin(), exp.getMax() + 1);
        // TODO 플레이어 지급
    }

    private void giveMoney(MobDTO.MobRewards.MobMoney money) {
        if (money == null)
            return;

        int amount = ThreadLocalRandom.current().nextInt(money.getMin(), money.getMax() + 1);
        // TODO 플레이어 지급
    }

    public MobDTO getMob() {
        return mob;
    }
}
