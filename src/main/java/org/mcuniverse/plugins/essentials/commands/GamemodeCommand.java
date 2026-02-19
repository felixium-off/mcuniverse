package org.mcuniverse.plugins.essentials.commands;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import org.mcuniverse.plugins.rank.permission.RequiresRank;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Optional;

public class GamemodeCommand {

    // 사용법: /gm <모드> [플레이어]
    @Command({"gamemode", "gm"})
    @RequiresRank("ADMIN")
    public void onGamemode(
            Player sender, 
            @Named("게임모드") GameMode mode,
            @Optional @Named("닉네임") EntityFinder finder
    ) {
        Player target = sender;

        // 타겟이 지정되었다면 해당 플레이어 찾기
        if (finder != null) {
            target = finder.findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("플레이어를 찾을 수 없습니다.");
                return;
            }
        }

        target.setGameMode(mode);
        
        if (target != sender) {
            sender.sendMessage(target.getUsername() + "님의 게임모드를 변경했습니다.");
        }
        target.sendMessage("게임모드가 " + mode.name() + "(으)로 변경되었습니다.");
    }
}