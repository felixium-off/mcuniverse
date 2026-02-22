package org.mcuniverse.plugins.cosmetics.api;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

public interface Cosmetic {
    String getId();             // 고유 ID (예: "title_admin", "trail_fire")
    String getName();           // 표시 이름
    CosmeticType getType();     // 종류 (TITLE, TRAIL...)
    Component getIcon();        // GUI에 표시될 아이콘/설명

    // 장착 시 실행될 로직 (예: 파티클 타이머 시작, 탭리스트 이름 변경)
    void onEquip(Player player);

    // 해제 시 실행될 로직 (예: 타이머 중지)
    void onUnequip(Player player);
}