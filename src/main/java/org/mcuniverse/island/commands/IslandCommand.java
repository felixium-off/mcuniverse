package org.mcuniverse.island.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import org.mcuniverse.island.GUI.IslandGUI;
import org.mcuniverse.island.GUI.IslandListGUI;
import org.mcuniverse.island.GUI.IslandMemberGUI;
import org.mcuniverse.island.Island;
import org.mcuniverse.island.manager.IslandManager;

public class IslandCommand extends Command {
    private final IslandGUI islandGUI;
    private final IslandManager islandManager;
    
    public IslandCommand(IslandManager islandManager) {
        super("island", "섬");
        this.islandManager = islandManager;
        this.islandGUI = new IslandGUI(islandManager);
        
        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                player.openInventory(islandGUI.getInventory());
            } else {
                sender.sendMessage("플레이어만 사용할 수 있는 명령어입니다.");
            }
        });

        ArgumentWord listArg = ArgumentType.Word("list").from("list");
        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                IslandListGUI islandListGUI = new IslandListGUI(islandManager);
                islandListGUI.open(player);
            } else {
                sender.sendMessage("플레이어만 사용할 수 있는 명령어입니다.");
            }
        }, listArg);

        // island members 서브커맨드
        ArgumentWord membersArg = ArgumentType.Word("members").from("members");
        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                Island island = islandManager.getIsland(player.getUuid());
                if (island == null) {
                    player.sendMessage(Component.text("섬이 없습니다! 먼저 섬을 생성하세요.")
                            .color(TextColor.color(0xFF0000)));
                } else {
                    IslandMemberGUI memberGUI = new IslandMemberGUI(islandManager, island);
                    memberGUI.open(player);
                }
            } else {
                sender.sendMessage("플레이어만 사용할 수 있는 명령어입니다.");
            }
        }, membersArg);
    }
}