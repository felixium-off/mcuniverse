package org.mcuniverse.scripts.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        super("gamemode", "gm");

        var creativeArg = ArgumentType.Literal("creative");
        var spectatorArg = ArgumentType.Literal("spectator");
        var survivalArg = ArgumentType.Literal("survival");
        var adventureArg = ArgumentType.Literal("adventure");

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                player.sendMessage("/gm creative");
                player.sendMessage("/gm survival");
                player.sendMessage("/gm spectator");
            }
        });

        addSyntax(((sender, context) -> {
            if (sender instanceof Player player) {
                player.setGameMode(GameMode.CREATIVE);
                player.sendMessage("자신의 게임모드를 크리에이티브 모드로 설정했습니다.");
            }
        }));

        addSyntax(((sender, context) -> {
            if (sender instanceof Player player) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage("자신의 게임모드를 고스트 모드로 설정했습니다.");
            }
        }));

        addSyntax(((sender, context) -> {
            if (sender instanceof Player player) {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage("자신의 게임모드를 서바이벌 모드로 설정했습니다.");
            }
        }));

        addSyntax(((sender, context) -> {
            if (sender instanceof Player player) {
                player.setGameMode(GameMode.ADVENTURE);
                player.sendMessage("자신의 게임모드를 어드벤처 모드로 설정했습니다.");
            }
        }));
    }
}
