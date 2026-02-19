package org.mcuniverse.plugins.essentials.warp;

import net.minestom.server.entity.Player;

import java.util.List;

public class WarpService {

    private final WarpStrategy strategy;

    public WarpService(WarpStrategy strategy) {
        this.strategy = strategy;
    }

    public void createWarp(String name, Player player) {
        // 인스턴스 식별자가 필요하다면 player.getInstance().getUniqueId().toString() 등을 사용
        Warp warp = new Warp(name, player.getPosition(), "default"); 
        strategy.createWarp(warp);
    }

    public void deleteWarp(String name) {
        strategy.deleteWarp(name);
    }

    public void teleport(Player player, String name) {
        Warp warp = strategy.getWarp(name);
        if (warp == null) return;
        
        // 같은 인스턴스라고 가정하고 텔레포트 (멀티월드 시 인스턴스 이동 로직 추가 필요)
        player.teleport(warp.position());
    }

    public List<String> getWarpNames() {
        return strategy.getWarpNames();
    }
    
    public boolean exists(String name) {
        return strategy.getWarp(name) != null;
    }
}