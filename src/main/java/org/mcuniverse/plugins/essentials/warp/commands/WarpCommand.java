package org.mcuniverse.plugins.essentials.warp.commands;

import net.minestom.server.entity.Player;
import org.mcuniverse.plugins.essentials.warp.WarpService;
import org.mcuniverse.plugins.rank.permission.RequiresRank;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;

public class WarpCommand {

    private final WarpService warpService;

    public WarpCommand(WarpService warpService) {
        this.warpService = warpService;
    }

    @Command("warp")
    @Subcommand("이동")
    @RequiresRank("ADMIN")
    public void onWarp(Player player, String name) {
        if (!warpService.exists(name)) {
            player.sendMessage("존재하지 않는 워프입니다.");
            return;
        }
        warpService.teleport(player, name);
        player.sendMessage(name + "(으)로 이동했습니다.");
    }

    @Command("warp")
    @Subcommand("생성")
    @RequiresRank("ADMIN")
    public void onCreate(Player player, String name) {
        if (warpService.exists(name)) {
            player.sendMessage("이미 존재하는 워프 이름입니다.");
            return;
        }
        warpService.createWarp(name, player);
        player.sendMessage("워프 '" + name + "'을(를) 생성했습니다.");
    }

    @Command("warp")
    @Subcommand("삭제")
    @RequiresRank("ADMIN")
    public void onDelete(Player player, String name) {
        if (!warpService.exists(name)) {
            player.sendMessage("존재하지 않는 워프입니다.");
            return;
        }
        warpService.deleteWarp(name);
        player.sendMessage("워프 '" + name + "'을(를) 삭제했습니다.");
    }

    @Command("warp")
    @Subcommand("리스트")
    @RequiresRank("ADMIN")
    public void onList(Player player) {
        player.sendMessage("워프 목록");
        for(String name : warpService.getWarpNames()) {
            player.sendMessage(name);
        }
    }
}