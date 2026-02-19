package org.mcuniverse.plugins.rank.commands;

import org.mcuniverse.plugins.rank.RankGroup;
import org.mcuniverse.plugins.rank.permission.RequiresRank;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Subcommand;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import org.mcuniverse.plugins.rank.RankService;

public class RankCommand {

    private final RankService rankService;

    public RankCommand(RankService rankService) {
        this.rankService = rankService;
    }

    @Command("rank")
    @Subcommand("set")
    @RequiresRank("ADMIN")
    public void onSet(Player player, @Named("target") EntityFinder finder, @Named("rank") RankGroup rank) {
        Player target = finder.findFirstPlayer(player);
        if (target == null) {
            player.sendMessage("해당 플레이어를 찾을 수 없습니다.");
            return;
        }
        rankService.setRank(target.getUuid(), rank);
        player.sendMessage(target.getUsername() + "님의 등급을 " + rank.getDisplayName() + "로 설정했습니다.");
    }
}
