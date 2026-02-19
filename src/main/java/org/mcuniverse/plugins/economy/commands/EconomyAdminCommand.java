package org.mcuniverse.plugins.economy.commands;

import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import org.mcuniverse.plugins.economy.Currency;
import org.mcuniverse.plugins.economy.EconomyService;
import org.mcuniverse.plugins.rank.permission.RequiresRank;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Subcommand;

public class EconomyAdminCommand {

    private final EconomyService economyService;

    public EconomyAdminCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    @Command("eco")
    @Subcommand("view")
    @RequiresRank("ADMIN")
    public void onView(Player player,
                       @Named("target") EntityFinder finder,
                       @Named("currency") Currency type) {
        Player target = finder.findFirstPlayer(player);
        if (target == null) {
            player.sendMessage("해당 플레이어를 찾을 수 없습니다.");
            return;
        }

        var targetName = target.getDisplayName();
        var targetAcct = economyService.getAccount(target.getUuid(), type);

        player.sendMessage(targetName + "님의" + targetAcct + "원 입니다.");
    }

    @Command("eco")
    @Subcommand({"give"})
    @RequiresRank("ADMIN")
    public void onDeposit(Player player,
                          @Named("currency") Currency type,
                          @Named("target") EntityFinder finder,
                          long amount) {
        Player target = finder.findFirstPlayer(player);
        if (target == null) {
            player.sendMessage("해당 플레이어를 찾을 수 없습니다.");
            return;
        }

        if (type.equals(Currency.CASH) || type.equals(Currency.BALANCE)) {
            economyService.deposit(target.getUuid(), type, amount)
                    .thenAccept(success -> {
                        if (success) {
                            player.sendMessage(target.getUsername() + "님에게 " + type.getDisplayName() + " " + amount + "원 입금이 완료되었습니다.");
                        }
                    });
        }
    }

    @Command("eco")
    @Subcommand({"take"})
    @RequiresRank("ADMIN")
    public void onWithdraw(Player player,
                           @Named("currency") Currency type,
                           @Named("target") EntityFinder finder,
                           long amount) {
        Player target = finder.findFirstPlayer(player);
        if (target == null) {
            player.sendMessage("해당 플레이어를 찾을 수 없습니다.");
            return;
        }

        // 비동기 결과 처리: 잔액을 먼저 조회하고 그 결과(balance)를 가지고 로직 수행
        economyService.getAccount(target.getUuid(), type)
                .thenAccept(balance -> {
                    if (amount > balance) {
                        // 잔액보다 많은 금액을 출금하려 할 때 -> 0으로 설정
                        economyService.setAccount(target.getUuid(), type, 0);
                        player.sendMessage(target.getUsername() + "님의 계좌 잔액이 부족하여 0으로 설정되었습니다.");
                    } else {
                        // 정상 출금
                        economyService.withdraw(target.getUuid(), type, amount).thenAccept(success -> {
                            if (success) {
                                player.sendMessage(target.getUsername() + "님의 계좌에서 " + amount + "원 출금이 완료되었습니다.");
                            }
                        });
                    }
                });
    }

    @Command("eco")
    @Subcommand({"reset"})
    @RequiresRank("ADMIN")
    public void onDeleteAccount(Player player, @Named("target") EntityFinder finder) {
        Player target = finder.findFirstPlayer(player);
        if (target == null) {
            player.sendMessage("해당 플레이어를 찾을 수 없습니다.");
            return;
        }

        economyService.deleteAccount(target.getUuid());
        target.sendMessage("계좌가 삭제되었습니다.");
        player.sendMessage(target + "님의 계좌가 삭제되었습니다.");
    }
}
