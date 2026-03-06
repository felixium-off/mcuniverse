package org.mcuniverse.systems.entity.handler;

import java.time.Instant;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import org.mcuniverse.systems.entity.tag.AnimalTags;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.item.ItemStack;

public class FarmAnimalInteractHandler {
    public void onInteract(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN)
            return;

        Entity target = event.getTarget();
        Player player = event.getPlayer();
        ItemStack hand = player.getItemInMainHand();

        if (isFeedItem(target.getEntityType(), hand.material())) {
            handleFeed(player, target, hand);
            return;
        }

        if (hand.material() == Material.AIR) {
            handlePet(player, target, hand);
        }
    }

    private boolean isFeedItem(EntityType type, Material item) {
        if (type == EntityType.CHICKEN || type == EntityType.SHEEP) {
            return item == Material.WHEAT_SEEDS;
        }

        if (type == EntityType.COW) {
            return item == Material.WHEAT;
        }

        if (type == EntityType.PIG || type == EntityType.RABBIT) {
            return item == Material.CARROT;
        }

        return false;
    }

    private Material getRewardItem(EntityType type) {
        if (type == EntityType.CHICKEN)
            return Material.EGG;
        return null;
    }

    private void handleFeed(Player player, Entity target, ItemStack hand) {
        int totalCount = target.getTag(AnimalTags.STACK_SIZE);
        int alreadyFed = target.getTag(AnimalTags.FED_COUNT);
        int hungryCount = totalCount - alreadyFed;

        if (hungryCount <= 0) {
            player.sendMessage(Component.text("배가 부른 무리입니다.").color(NamedTextColor.YELLOW));
            return;
        }

        int feedCount = Math.min(hand.amount(), hungryCount);
        player.setItemInMainHand(hand.withAmount(hand.amount() - feedCount));

        int newFed = alreadyFed + feedCount;
        target.setTag(AnimalTags.FED_COUNT, newFed);

        Material reward = getRewardItem(target.getEntityType());
        if (reward != null) {
            player.getInventory().addItemStack(ItemStack.of(reward, feedCount));
        }
        updateDisplayName(target, totalCount, newFed);
        player.sendMessage(Component.text(feedCount + "마리에게 밥을 줬습니다!")
                .color(NamedTextColor.GREEN));
    }

    private void handlePet(Player player, Entity target, ItemStack hand) {
        long now = Instant.now().getEpochSecond();
        long lastPetted = target.getTag(AnimalTags.LAST_PETTED_TIME);
        long secondsInDay = 86_400L;

        if (now - lastPetted < secondsInDay) {
            player.sendMessage(Component.text("오늘은 이미 쓰다듬었어요!").color(NamedTextColor.YELLOW));
            return;
        }

        int affection = target.getTag(AnimalTags.AFFECTION);
        if (affection < 10) {
            target.setTag(AnimalTags.AFFECTION, affection + 1);
        }
        target.setTag(AnimalTags.LAST_PETTED_TIME, now);

        player.sendMessage(Component.text("호감도 : " + (affection + 1) + " / 10 ").color(NamedTextColor.LIGHT_PURPLE));
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
