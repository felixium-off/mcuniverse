package org.mcuniverse;

import net.minestom.server.MinecraftServer;

public class Main {

    public static void main(String[] args) {
        createServer();
    }

    private static void createServer() {

        MinecraftServer minecraftServer = MinecraftServer.init();
        minecraftServer.LOGGER.debug("서버가 안전하게 열렸습니다.");

        // 종료 작업 등록
        disableServer(minecraftServer);
        minecraftServer.start("0.0.0.0", 25565);
    }

    /**
     * 서버 종료 작업
     * DB 저장 작업
     * @param minecraftServer
     */
    private static void disableServer(MinecraftServer minecraftServer) {
        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            minecraftServer.LOGGER.debug("서버가 안전하게 종료되었습니다.");
        });
    }
}