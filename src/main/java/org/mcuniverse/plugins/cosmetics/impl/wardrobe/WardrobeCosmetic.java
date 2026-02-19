package org.mcuniverse.plugins.cosmetics.impl.wardrobe;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.mcuniverse.plugins.cosmetics.api.Cosmetic;
import org.mcuniverse.plugins.cosmetics.api.CosmeticType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WardrobeCosmetic implements Cosmetic {

    private final String id;
    private final String name;
    private final CosmeticType type; // HAT, BACK, BALLOON 등
    private final int modelData; // 리소스팩 CustomModelData ID

    // 플레이어별 소환된 엔티티 관리 (메모리 누수 방지 필수)
    private final Map<UUID, Entity> spawnedEntities = new ConcurrentHashMap<>();

    public WardrobeCosmetic(String id, String name, CosmeticType type, int modelData) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.modelData = modelData;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public CosmeticType getType() { return type; }

    @Override
    public Component getIcon() {
        return Component.text(name); // 실제로는 아이템 아이콘 반환
    }

    @Override
    public void onEquip(Player player) {
        // 1. 아이템 디스플레이 엔티티 생성 (블록벤치 모델용)
        Entity displayEntity = new Entity(EntityType.ITEM_DISPLAY);
        ItemDisplayMeta meta = (ItemDisplayMeta) displayEntity.getEntityMeta();
        
        // 2. 리소스팩 모델 적용
        ItemStack item = ItemStack.of(Material.PAPER);
        meta.setItemStack(item);

        // 3. 위치 설정 및 소환 (실제로는 태스크로 플레이어 위치를 따라가거나 Passenger 설정)
        displayEntity.setInstance(player.getInstance(), player.getPosition().add(0, 2, 0));
        spawnedEntities.put(player.getUuid(), displayEntity);
    }

    @Override
    public void onUnequip(Player player) {
        Entity entity = spawnedEntities.remove(player.getUuid());
        if (entity != null) {
            entity.remove();
        }
    }
}