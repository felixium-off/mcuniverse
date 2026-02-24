package org.mcuniverse.systems.entity.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.mcuniverse.Server;
import org.mcuniverse.systems.entity.data.MobDTO;
import org.mcuniverse.systems.entity.model.BaseMob;
import org.mcuniverse.systems.entity.model.BossMob;
import org.mcuniverse.systems.entity.model.CommonMob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class MobService {

    private static Logger log = LoggerFactory.getLogger(MobService.class);

    public BaseMob spawnMob(String mobId, Instance instance, Pos pos) {
        return spawnMob(mobId, instance, pos, null);
    }

    public BaseMob spawnMob(String mobId, Instance instance, Pos pos, Consumer<BaseMob> modifier) {
        MobDTO mob = Server.MOB_MANAGER.getMob(mobId);
        if (mob == null) {
            log.info("[MobService] 유효하지 않은 몬스터 ID: {}", mobId);
            return null;
        }

        BaseMob spawnEntity;

        if ("BOSS".equalsIgnoreCase(mob.getGroup())) {
            spawnEntity = new BossMob(mob);
        } else {
            spawnEntity = new CommonMob(mob);
        }

        if (modifier != null)
            modifier.accept(spawnEntity);

        spawnEntity.setInstance(instance, pos);
        return spawnEntity;
    }
}
