package org.mcuniverse.systems.entity.model;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.entity.EntityDamageEvent;
import org.mcuniverse.systems.entity.data.MobDTO;

public class BossMob extends BaseMob {

    private final BossBar bossBar;

    public BossMob(MobDTO mob) {
        super(mob);
        this.bossBar = BossBar.bossBar(
                Component.text(mob.getDisplayName() + " (BOSS)"),
                1.0f, // 1.0f 가 100% 꽉찬 상태
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS);

        onDamagedBoss();
        applyBossPattern();
    }

    private void onDamagedBoss() {
        eventNode().addListener(EntityDamageEvent.class, event -> {
            float maxHealth = mob.getStats().getHp();
            float expectedHealth = getHealth() - event.getDamage().getAmount();
            float percent = Math.max(0.0f, expectedHealth / maxHealth);
            bossBar.progress(percent);
        });
    }

    private void applyBossPattern() {
        if (getMob().getScriptHook() != null) {
            // 나중에 보스 패턴을 만들때 추가해야 합니다.
        }
    }

    @Override
    public void updateNewViewer(Player player) {
        super.updateNewViewer(player);
        player.showBossBar(bossBar); // 플레이어가 보스 근처에 오면 보스바 표시
    }

    @Override
    public void updateOldViewer(Player player) {
        super.updateOldViewer(player);
        player.hideBossBar(bossBar); // 플레이어가 보스에게서 멀어지면 보스바 숨김
    }

    @Override
    public void remove() {
        super.remove();
        // 보스가 죽거나 삭제될 때, 보스바를 보던 모든 시청자의 화면에서 강제로 꺼줌
        for (Player viewer : getViewers()) {
            viewer.hideBossBar(bossBar);
        }
    }
}
