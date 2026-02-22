package org.mcuniverse;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.network.packet.server.common.PingPacket;

import org.mcuniverse.core.database.mongo.MongoConnect;
import org.mcuniverse.core.database.redis.RedisConnect;
import org.mcuniverse.scripts.event.PingProtectEvent;
import org.mcuniverse.scripts.event.PlayerJoinEvent;
import org.mcuniverse.systems.resourcepack.ResourcepackConfig;
import org.mcuniverse.systems.resourcepack.ResourcepackService;
import org.mcuniverse.systems.world.WorldService;

public final class Server {

    private static MinecraftServer minecraftServer;

    private WorldService worldService;
    private ResourcepackService resourcepackService;

    public void start() {
        this.initMinestorm();
        this.initServices();
        this.connectDB();
        this.registerCommands();
        this.registerEvents();
        this.registerShutdownHook();
        this.minecraftServer.start("0.0.0.0", 25565);
    }

    public void initMinestorm() {
        this.minecraftServer = MinecraftServer.init();
        this.minecraftServer.LOGGER.info("서버가 안전하게 열렸습니다.");
    }

    public void initServices() {
        ResourcepackConfig config = new ResourcepackConfig();
        this.resourcepackService = new ResourcepackService(config);
        this.worldService = new WorldService();
    }

    public void connectDB() {
        MongoConnect.getInstance().connect();
        RedisConnect.getInstance().connect();
    }

    public void registerCommands() {

    }

    public void registerEvents() {
        GlobalEventHandler gh = MinecraftServer.getGlobalEventHandler();
        new PlayerJoinEvent(this.worldService, this.resourcepackService).register(gh);
        new PingProtectEvent().register(gh);
    }

    public void registerShutdownHook() {
        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {

            MongoConnect.getInstance().disconnect();
            RedisConnect.getInstance().disconnect();
            this.minecraftServer.LOGGER.debug("서버가 안전하게 종료되었습니다.");
        });
    }
}
