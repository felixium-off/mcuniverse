package org.mcuniverse.systems.entity.api;

import java.util.HashSet;
import java.util.Set;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;

public class GarbageEntityCleaner {

    private static final Logger log = LoggerFactory.getLogger(GarbageEntityCleaner.class);

    private final int REPEAT_INTERVAL = 60;

    public void start() {
        MinecraftServer.getSchedulerManager().buildTask(this::sweep)
                .repeat(TaskSchedule.seconds(REPEAT_INTERVAL))
                .schedule();

        log.info("[GEC] Garbage Entity Cleaner started (30초 주기)");
    }

    private void sweep() {
        Set<Instance> instances = new HashSet<>();
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> {
            if (p.getInstance() != null)
                instances.add(p.getInstance());
        });

        int removed = 0;
        for (Instance instance : instances) {
            for (Entity entity : instance.getEntities()) {
                if (isGarbage(entity)) {
                    entity.remove();
                    removed++;
                }
            }
        }

        if (removed > 0)
            log.info("[GEC] swept {} garbage entities", removed);
    }

    private boolean isGarbage(Entity entity) {
        if (entity.getInstance() == null) {
            return true;
        }

        if (entity.getEntityType() == EntityType.TEXT_DISPLAY) {
            return false;
        }

        long aliveMillis = System.currentTimeMillis() - entity.getAliveTicks() * 50L;
        return aliveMillis > 5000;
    }
}
