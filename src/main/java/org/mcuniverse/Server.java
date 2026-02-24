package org.mcuniverse;

import lombok.extern.slf4j.Slf4j;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import org.mcuniverse.core.database.mongo.MongoConnect;
import org.mcuniverse.core.database.redis.RedisConnect;
import org.mcuniverse.scripts.event.MobCombatEvent;
import org.mcuniverse.scripts.event.PingProtectEvent;
import org.mcuniverse.scripts.event.PlayerJoinEvent;
import org.mcuniverse.systems.entity.command.EntityCommand;
import org.mcuniverse.systems.entity.mob.MobManager;
import org.mcuniverse.systems.entity.mob.MobService;
import org.mcuniverse.systems.resourcepack.ResourcepackConfig;
import org.mcuniverse.systems.resourcepack.ResourcepackService;
import org.mcuniverse.systems.world.WorldService;

@Slf4j
public final class Server {

    private static MinecraftServer minecraftServer;

    private WorldService worldService;
    private MobService mobService;
    private ResourcepackService resourcepackService;

    public static final MobManager MOB_MANAGER = new MobManager();

    public void start() {
        this.initMinestorm();
        this.initServices();
        this.connectDB();
        this.registerCommands();
        this.registerEvents();
        this.registerFeatures();
        this.registerShutdownHook();
        minecraftServer.start("0.0.0.0", 25565);
    }

    public void initMinestorm() {
        minecraftServer = MinecraftServer.init();
        log.info("서버가 안전하게 열렸습니다.");
    }

    public void initServices() {
        ResourcepackConfig config = new ResourcepackConfig();
        this.resourcepackService = new ResourcepackService(config);
        this.worldService = new WorldService();
        this.mobService = new MobService();
    }

    public void connectDB() {
        MongoConnect.getInstance().connect();
        RedisConnect.getInstance().connect();
    }

    public void registerCommands() {
        MinecraftServer.getCommandManager().register(new EntityCommand(mobService));
    }

    public void registerEvents() {
        GlobalEventHandler gh = MinecraftServer.getGlobalEventHandler();
        new PlayerJoinEvent(this.worldService, this.resourcepackService).register(gh);
        new MobCombatEvent().register(gh);
        new PingProtectEvent().register(gh);
    }

    public void registerFeatures() {
        MOB_MANAGER.loadAllMobs();
    }

    public void registerShutdownHook() {
        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {

            MongoConnect.getInstance().disconnect();
            RedisConnect.getInstance().disconnect();
            log.debug("서버가 안전하게 종료되었습니다.");
        });
    }
}
