package org.mcuniverse.island.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import org.mcuniverse.island.Island;
import org.mcuniverse.island.generate.IslandGenerator;
import org.mcuniverse.managers.SpawnManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IslandManager {
    private final Map<UUID, Island> playerIslands = new ConcurrentHashMap<>();
    private final Map<String, Island> islandsById = new ConcurrentHashMap<>();
    private final InstanceManager instanceManager;
    
    public IslandManager() {
        this.instanceManager = MinecraftServer.getInstanceManager();
    }
    
    /**
     * 플레이어의 섬을 생성합니다.
     */
    public Island createIsland(UUID playerUuid) {
        // 이미 섬이 있으면 반환
        if (playerIslands.containsKey(playerUuid)) {
            return playerIslands.get(playerUuid);
        }
        
        // 새 인스턴스 생성
        InstanceContainer instance = instanceManager.createInstanceContainer();
        
        // 섬 생성기 설정 (Minestom의 Generator 함수형 인터페이스 사용)
        instance.setGenerator(IslandGenerator::generateIsland);
        instance.setChunkSupplier(LightingChunk::new);
        
        // 섬 ID 생성
        String islandId = playerUuid.toString();
        
        // 스폰 위치 설정 (섬 중앙)
        Pos spawnPoint = new Pos(0, 65, 0);
        
        // 섬 객체 생성
        Island island = new Island(playerUuid, islandId, instance, spawnPoint);
        
        // 저장
        playerIslands.put(playerUuid, island);
        islandsById.put(islandId, island);
        
        return island;
    }
    
    /**
     * 플레이어의 섬을 가져옵니다.
     */
    public Island getIsland(UUID playerUuid) {
        return playerIslands.get(playerUuid);
    }
    
    /**
     * 섬을 삭제합니다.
     * 섬의 모든 멤버를 메인 spawn으로 이동시킵니다.
     */
    public boolean deleteIsland(UUID playerUuid) {
        Island island = playerIslands.remove(playerUuid);
        if (island != null) {
            islandsById.remove(island.getIslandId());
            
            // 섬의 모든 멤버를 메인 spawn으로 이동
            List<UUID> members = island.getMembers();
            for (UUID memberUuid : members) {
                Player member = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(memberUuid);
                if (member != null) {
                    // 멤버가 현재 섬 인스턴스에 있으면 메인 spawn으로 이동
                    if (member.getInstance() == island.getInstance()) {
                        SpawnManager.teleportToSpawn(member);
                        member.sendMessage(net.kyori.adventure.text.Component.text("섬이 삭제되어 메인 spawn으로 이동했습니다.")
                                .color(net.kyori.adventure.text.format.TextColor.color(0xFF0000)));
                    }
                }
            }
            
            // 인스턴스 정리 (필요시)
            return true;
        }
        return false;
    }
    
    /**
     * 플레이어가 섬을 가지고 있는지 확인합니다.
     */
    public boolean hasIsland(UUID playerUuid) {
        return playerIslands.containsKey(playerUuid);
    }
    
    /**
     * 모든 섬 목록을 가져옵니다.
     */
    public Collection<Island> getAllIslands() {
        return playerIslands.values();
    }
}