package org.mcuniverse.plugins.user.commands;

import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import org.mcuniverse.plugins.rank.permission.RequiresRank;
import org.mcuniverse.plugins.user.UserService;
import revxrsal.commands.annotation.Command;

public class UserCommand {

    private final UserService userService;

    public UserCommand(UserService userService) {
        this.userService = userService;
    }

    @Command("ban")
    @RequiresRank("ADMIN")
    public void onBan(Player player, EntityFinder finder) {
        Player target = finder.findFirstPlayer(player);
        userService.lockedUser(target.getUuid(), true);
        player.sendMessage(target.getUsername() + "님을 블랙리스트에 추가하였습니다.");
        player.kick("블랙리스트에 포함 되었습니다. 자세한 내용은 운영진에게 문의 해주세요.");
    }

    @Command("unban")
    @RequiresRank("ADMIN")
    public void onUnban(Player player, EntityFinder finder) {
        Player target = finder.findFirstPlayer(player);
        userService.lockedUser(target.getUuid(), false);
        player.sendMessage(target.getUsername() + "님의 블랙리스트를 해제하였습니다.");
    }
}
