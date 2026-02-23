package org.mcuniverse.systems.entity.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.mcuniverse.Server;
import org.mcuniverse.systems.entity.data.MobDTO;
import org.mcuniverse.systems.entity.model.CustomMob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class MobService {

    private static Logger log = LoggerFactory.getLogger(MobService.class);

    public CustomMob spawnMob(String mobId, Instance instance, Pos pos) {
        return spawnMob(mobId, instance, pos, null);
    }

    public CustomMob spawnMob(String mobId, Instance instance, Pos pos, Consumer<CustomMob> modifier) {
        MobDTO mob = Server.MOB_MANAGER.getMob(mobId);
        if (mob == null) {
            log.info("[MobService] 유효하지 않은 몬스터 ID: {}", mobId);
            return null;
        }

        CustomMob customMob = new CustomMob(mob);
        if (modifier != null) modifier.accept(customMob);

        customMob.setInstance(instance, pos);
        return customMob;
    }
}
