package org.mcuniverse.plugins.cosmetics.impl.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import org.mcuniverse.plugins.cosmetics.api.Cosmetic;
import org.mcuniverse.plugins.cosmetics.api.CosmeticType;

public class ColorChatCosmetic implements Cosmetic {

    private final String id;
    private final NamedTextColor color;

    public ColorChatCosmetic(String id, NamedTextColor color) {
        this.id = id;
        this.color = color;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return color.toString() + " Chat"; }

    @Override
    public CosmeticType getType() { return CosmeticType.COLOR_CHAT; }

    @Override
    public Component getIcon() { return Component.text("A").color(color); }

    @Override
    public void onEquip(Player player) {
        // 방법 1: 플레이어 태그/메타데이터에 색상 정보 저장
        // 추후 ChatListener에서 player.getTag(Tag.String("chat_color"))를 확인하여 적용
        player.setTag(net.minestom.server.tag.Tag.String("chat_color"), color.asHexString());
        
        // 안내 메시지
        player.sendMessage(Component.text("채팅 색상이 변경되었습니다.", color));
    }

    @Override
    public void onUnequip(Player player) {
        player.removeTag(net.minestom.server.tag.Tag.String("chat_color"));
    }
}