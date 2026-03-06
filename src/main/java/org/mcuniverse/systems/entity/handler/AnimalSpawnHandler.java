package org.mcuniverse.systems.entity.handler;

import java.util.Optional;

import org.mcuniverse.systems.entity.data.MobDTO;
import org.mcuniverse.systems.entity.model.CustomMob;
import org.mcuniverse.systems.entity.tag.AnimalTags;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class AnimalSpawnHandler {

    public void onBlockInteract(PlayerBlockInteractEvent event, MobDTO mob, CustomMob customMob) {
        Player player = event.getPlayer();
        ItemStack hand = player.getItemInMainHand();

        if (hand.material() != Material.EGG)
            return;

        Optional<Entity> existngHerd = player.getInstance().getEntities().stream()
                .filter(e -> mob.getId().equals(e.getTag(AnimalTags.OWNER_UUID))).findFirst();

        if (existngHerd.isPresent()) {
            event.setCancelled(true);
            player.sendMessage(Component.text(
                    "이미 섬에 " + mob.getDisplayName() + " 무리가 있습니다. " +
                            "기존 무리를 우클릭하여 마릿수를 늘려주세요!")
                    .color(NamedTextColor.YELLOW));
            return;
        }

        customMob.setTag(AnimalTags.STACK_SIZE, 1);
        customMob.setTag(AnimalTags.OWNER_UUID, player.getUuid().toString());
        customMob.setInstance(player.getInstance(), event.getBlockPosition().asVec().add(0.5, 1, 0.5));

        player.setItemInMainHand(hand.withAmount(hand.amount() - 1));

        updateDisplayName(customMob, 1, 0);
        player.sendMessage(Component.text(mob.getDisplayName() + " 무리를 소환했습니다!")
                .color(NamedTextColor.GREEN));
    }

    public void onEntityInteract(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN)
            return;

        Player player = event.getPlayer();
        Entity target = event.getTarget();
        ItemStack hand = player.getItemInMainHand();

        if (hand.material() != Material.AIR)
            return;

        String ownerUuid = target.getTag(AnimalTags.OWNER_UUID);
        if (!player.getUuid().toString().equals(ownerUuid)) {
            player.sendMessage(Component.text("내 가축이 아닙니다!")
                    .color(NamedTextColor.RED));
            return;
        }

        addToStack(player, target, hand);
    }

    private void addToStack(Player player, Entity target, ItemStack hand) {
        int current = target.getTag(AnimalTags.STACK_SIZE);
        int newSize = current + 1;

        target.setTag(AnimalTags.STACK_SIZE, newSize);

        player.setItemInMainHand(hand.withAmount(hand.amount() - 1));

        int fed = target.getTag(AnimalTags.FED_COUNT);
        updateDisplayName(target, newSize, fed);
        player.sendMessage(Component.text("무리가 " + newSize + "마리가 됐습니다!")
                .color(NamedTextColor.AQUA));
    }

    private void updateDisplayName(Entity entity, int total, int fed) {
        int hungry = total - fed;
        Component nameTag = Component.empty();

        if (hungry > 0) {
            nameTag = nameTag.append(
                    Component.text("🐔 배고픈 [x" + hungry + "] ").color(NamedTextColor.GRAY));
        }
        if (hungry > 0 && fed > 0) {
            nameTag = nameTag.append(
                    Component.text("| ").color(NamedTextColor.DARK_GRAY));
        }
        if (fed > 0) {
            nameTag = nameTag.append(
                    Component.text("♥ 행복한 [x" + fed + "]").color(NamedTextColor.GREEN));
        }
        entity.setCustomName(nameTag);
        entity.setCustomNameVisible(true);
    }

}
