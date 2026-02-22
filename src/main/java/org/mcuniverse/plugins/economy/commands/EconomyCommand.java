package org.mcuniverse.plugins.economy.commands;

import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import org.mcuniverse.plugins.economy.Currency;
import org.mcuniverse.plugins.economy.EconomyService;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Subcommand;

import java.util.concurrent.CompletableFuture;

public class EconomyCommand {

    private final EconomyService economyService;

    public EconomyCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    // --- [ 일반 유저 명령어: /돈 ] ---

    @Command("돈")
    @Description("경제 시스템 명령어입니다.")
    public void onHelp(Player player) {
        player.sendMessage("재화 명령어 안내");
        player.sendMessage("/돈 잔액 - 내 잔액을 확인합니다.");
        player.sendMessage("/돈 보내기 [닉네임] [금액] - 다른 플레이어에게 돈을 보냅니다.");
    }

    @Command("돈")
    @Subcommand("잔액")
    public void onBalance(Player player) {
        CompletableFuture<Long> balance = economyService.getAccount(player.getUuid(), Currency.BALANCE);
        player.sendMessage("잔액: " + balance + "원");
    }

    @Command("돈")
    @Subcommand("보내기")
    public void onPay(Player player, @Named("닉네임") EntityFinder finder, @Named("금액") long amount) {
        if (amount <= 0) {
            player.sendMessage("금액은 0보다 커야 합니다.");
            return;
        }

        Player target = finder.findFirstPlayer(player);
        if (target == null) {
            player.sendMessage("해당 플레이어를 찾을 수 없습니다.");
            return;
        }

        if (target.getUuid().equals(player.getUuid())) {
            player.sendMessage("자신에게 돈을 보낼 수 없습니다.");
            return;
        }

        economyService.getAccount(player.getUuid(), Currency.BALANCE)
                .thenAccept(balance -> {
                    if (balance < amount) {
                        player.sendMessage("잔액이 부족합니다.");
                    } else {
                        economyService.withdraw(player.getUuid(), Currency.BALANCE,  amount);
                        player.sendMessage(target.getUsername() + "님에게 " + amount + "원을 보냈습니다.");

                        economyService.deposit(target.getUuid(), Currency.BALANCE, amount);
                        target.sendMessage(player.getUsername() + "님으로부터 " + amount + "원을 받았습니다.");
                    }
                });
    }

    @Command("캐쉬")
    public void onCash(Player player) {
        economyService.getAccount(player.getUuid(), Currency.CASH)
                .thenAccept(cash -> {
                    player.sendMessage("잔액" + cash);
                });
    }
}
