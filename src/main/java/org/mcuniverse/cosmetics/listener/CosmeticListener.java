package org.mcuniverse.cosmetics.listener;

import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.mcuniverse.cosmetics.manager.CosmeticManager;

public class CosmeticListener {

    public CosmeticListener(CosmeticManager manager, GlobalEventHandler handler) {
        
        // 플레이어 스폰(접속) 시 데이터 로드 및 적용
        handler.addListener(PlayerSpawnEvent.class, event -> {
            // 비동기로 로드하는 것이 좋으나, 예시를 위해 동기 처리
            // 실제로는 CompletableFuture 등을 사용하여 로드 후 메인 스레드에서 equip 호출 권장
            manager.loadData(event.getPlayer());
        });

        // 플레이어 퇴장 시 데이터 저장 및 해제
        handler.addListener(PlayerDisconnectEvent.class, event -> {
            // 1. 데이터 저장
            manager.saveData(event.getPlayer());
            
            // 2. 장착 해제 (엔티티 제거 등)
            manager.unequipAll(event.getPlayer());
        });
    }
}