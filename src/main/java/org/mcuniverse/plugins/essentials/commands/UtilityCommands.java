package org.mcuniverse.plugins.essentials.commands;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import org.mcuniverse.plugins.rank.permission.RequiresRank;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Range;

public class UtilityCommands {

    @Command({"fly"})
    @RequiresRank("ADMIN")
    public void onFly(Player player) {
        boolean allow = !player.isAllowFlying();
        player.setAllowFlying(allow);
        player.setFlying(allow);
        player.sendMessage("비행 모드: " + (allow ? "켜짐" : "꺼짐"));
    }

    @Command({"heal"})
    @RequiresRank("ADMIN")
    public void onHeal(Player player) {
        player.heal();
        player.setFood(20);
        player.sendMessage("체력과 배고픔이 회복되었습니다.");
    }

    @Command({"speed"})
    @RequiresRank("ADMIN")
    public void onSpeed(Player player, @Range(min = 0.1, max = 10.0) float speed) {
        // Minestom에서 기본 속도는 0.1입니다. 입력값을 적절히 조절합니다.
        float finalSpeed = speed / 10.0f; 
        
        if (player.isFlying()) {
            player.setFlyingSpeed(finalSpeed);
            player.sendMessage("비행 속도가 " + speed + "(으)로 설정되었습니다.");
        } else {
            // 걷기 속도 설정 (Attribute 사용 권장되나 간단한 구현을 위해 필드 사용)
            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(finalSpeed);
            player.sendMessage("이동 속도가 " + speed + "(으)로 설정되었습니다.");
        }
    }
}