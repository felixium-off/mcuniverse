package org.mcuniverse.plugins.cosmetics.impl.title;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.mcuniverse.plugins.cosmetics.api.Cosmetic;
import org.mcuniverse.plugins.cosmetics.api.CosmeticType;

public class TitleCosmetic implements Cosmetic {
    private final String id;
    private final String content; // 실제 칭호 내용 (예: "[관리자]")

    public TitleCosmetic(String id, String content) {
        this.id = id;
        this.content = content;
    }

    // 생성자...

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public CosmeticType getType() {
        return null;
    }

    @Override
    public Component getIcon() {
        return null;
    }

    @Override
    public void onEquip(Player player) {
        // 예: 플레이어의 커스텀 이름 변경 로직
        // player.setCustomName(...);
        // 또는 ChatManager에 칭호 데이터 주입
    }

    @Override
    public void onUnequip(Player player) {
        // 칭호 제거 로직
    }
}