package org.mcuniverse.scripts.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityDamageEvent;

import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import org.mcuniverse.systems.entity.combat.CombatStats;
import org.mcuniverse.systems.entity.combat.DamageFormula;
import org.mcuniverse.systems.entity.model.BaseMob;
import org.mcuniverse.systems.entity.model.CustomPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minestom.server.event.GlobalEventHandler;

import java.util.Random;

public class MobCombatEvent {

    private static final Logger log = LoggerFactory.getLogger(MobCombatEvent.class);

    private final Random random = new Random();

    public void register(GlobalEventHandler eventNode) {
        eventNode.addListener(EntityAttackEvent.class, this::onEntityAttack);
        eventNode.addListener(EntityDamageEvent.class, this::onEntityDamage);
    }

    // 몬스터가 플레이어를 공격할 때 이벤트
    private void onEntityAttack(EntityAttackEvent event) {
        var attacker = event.getEntity();
        var target = event.getTarget();

        // 플레이어가 몬스터를 공격할 때
        if (attacker instanceof CustomPlayer player && target instanceof BaseMob mob) {
            CombatStats attackerStats = CombatStats.fromPlayerStats(player.getStats());
            CombatStats defenderStats = CombatStats.fromMobStats(mob.getMob().getStats());

            float calculateDamage = DamageFormula.calculateDamage(attackerStats, defenderStats);
            Damage damage = new Damage(DamageType.MOB_ATTACK, mob, attacker, null, calculateDamage);
            mob.damage(damage);
        }

        // 몬스터가 플레이어를 공격할 때
        if (attacker instanceof BaseMob mob && target instanceof CustomPlayer player) {
            CombatStats attackerStats = CombatStats.fromMobStats(mob.getMob().getStats());
            CombatStats defenderStats = CombatStats.fromPlayerStats(player.getStats());

            float damage = DamageFormula.calculateDamage(attackerStats, defenderStats);
            player.damage(new Damage(DamageType.MOB_ATTACK, mob, attacker, null, damage));
        }
    }

    // 몬스터가 데미지를 입었을 때 이벤트
    private void onEntityDamage(EntityDamageEvent event) {
        var victim = event.getEntity();
        float incomingDamage = event.getDamage().getAmount();

        showDamageIndicator(victim, incomingDamage);

        if (victim instanceof BaseMob mob) { // 몬스터가 맞았을 때
            if ((mob.getHealth() - incomingDamage) <= 0) {
                onMobKilled(mob);
            }
        } else if (victim instanceof Player player) { // 플레이어가 맞았을 때
            player.sendPacket(
                    new EntityAnimationPacket(player.getEntityId(),
                            EntityAnimationPacket.Animation.TAKE_DAMAGE));
            player.sendMessage(incomingDamage + "데미지" + victim.getHealth() + "hp");
        }
    }

    private void showDamageIndicator(Entity victim, float damage) {
        if (damage <= 0 || victim.getInstance() == null)
            return;

        Entity indicator = new Entity(EntityType.TEXT_DISPLAY);
        indicator.setNoGravity(true);

        TextDisplayMeta meta = (TextDisplayMeta) indicator.getEntityMeta();
        String damageText = String.format("%.1f", damage);

        meta.setText(Component.text(damageText, NamedTextColor.RED).decorate(TextDecoration.BOLD));
        meta.setSeeThrough(false);
        meta.setUseDefaultBackground(false);
        meta.setBackgroundColor(0);
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);

        double offsetX = (random.nextDouble() - 0.5) * 1.5;
        double offsetZ = (random.nextDouble() - 0.5) * 1.5;

        Pos displayPos = victim.getPosition().add(offsetX, 1.3, offsetZ);
        indicator.setInstance(victim.getInstance(), displayPos);

        MinecraftServer.getSchedulerManager().buildTask(indicator::remove)
                .delay(TaskSchedule.millis(800))
                .schedule();
    }

    // 몬스터가 죽었을 때 이벤트
    private void onMobKilled(BaseMob mob) {
        log.info("[MobAttackEvent] 몬스터가 죽었습니다: {}", mob.getMob().getId());
        showDeathEffect(mob);
        mob.remove();
    }

    private void showDeathEffect(Entity entity) {
        ParticlePacket pp = new ParticlePacket(Particle.CLOUD,
                entity.getPosition().x(), entity.getPosition().y() + 1.0f, entity.getPosition().z(),
                0.5f, 0.5f, 0.5f, 0.1f, 10);
        entity.sendPacketToViewers(pp);
    }
}
