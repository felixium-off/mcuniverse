package org.mcuniverse.island.GUI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.mcuniverse.island.Island;
import org.mcuniverse.island.manager.IslandManager;

import java.util.UUID;

public class IslandMemberManageGUI {
    private final IslandManager islandManager;
    private final Island island;
    private final UUID targetUuid;
    private final Inventory inventory;
    private final EventNode<InventoryEvent> eventNode;
    
    public IslandMemberManageGUI(IslandManager islandManager, Island island, UUID targetUuid) {
        this.islandManager = islandManager;
        this.island = island;
        this.targetUuid = targetUuid;
        this.inventory = new Inventory(InventoryType.CHEST_1_ROW, 
                Component.text("멤버 관리: " + getPlayerName(targetUuid)));
        
        this.eventNode = EventNode.type("islandMemberManageGUI", EventFilter.INVENTORY,
                (event, inv) -> inv == inventory);
        
        setupInventory();
        setupEventListeners();
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);
    }
    
    private void setupInventory() {
        Island.MemberRole currentRole = island.getMemberRole(targetUuid);
        
        // 추방 버튼
        ItemStack kickItem = ItemStack.builder(Material.LAVA_BUCKET)
                .customName(Component.text("멤버 추방")
                        .color(TextColor.color(0xFF0000)))
                .lore(Component.text("이 멤버를 섬에서 추방합니다."))
                .build();
        inventory.setItemStack(2, kickItem);
        
        // 진급 버튼 (일반 멤버인 경우만)
        if (currentRole == Island.MemberRole.MEMBER) {
            ItemStack promoteItem = ItemStack.builder(Material.EMERALD)
                    .customName(Component.text("관리자로 진급")
                            .color(TextColor.color(0x00FF00)))
                    .lore(Component.text("이 멤버를 관리자로 승격시킵니다."))
                    .build();
            inventory.setItemStack(4, promoteItem);
        } else if (currentRole == Island.MemberRole.MODERATOR) {
            ItemStack demoteItem = ItemStack.builder(Material.IRON_INGOT)
                    .customName(Component.text("일반 멤버로 강등")
                            .color(TextColor.color(0xAAAAAA)))
                    .lore(Component.text("이 멤버를 일반 멤버로 강등시킵니다."))
                    .build();
            inventory.setItemStack(4, demoteItem);
        }
        
        // 귓속말 버튼
        ItemStack whisperItem = ItemStack.builder(Material.PAPER)
                .customName(Component.text("귓속말")
                        .color(TextColor.color(0xFFFF00)))
                .lore(Component.text("이 플레이어에게 귓속말을 보냅니다."))
                .build();
        inventory.setItemStack(6, whisperItem);
        
        // 닫기 버튼
        ItemStack closeItem = ItemStack.builder(Material.BARRIER)
                .customName(Component.text("닫기")
                        .color(TextColor.color(0xFF0000)))
                .build();
        inventory.setItemStack(8, closeItem);
    }
    
    private String getPlayerName(UUID uuid) {
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
        if (player != null) {
            return player.getUsername();
        }
        return "플레이어_" + uuid.toString().substring(0, 8);
    }
    
    private void setupEventListeners() {
        eventNode.addListener(EventListener.builder(InventoryPreClickEvent.class)
                .filter(event -> event.getClick() instanceof Click.Left)
                .handler(event -> {
                    event.setCancelled(true);
                    Player player = event.getPlayer();
                    ItemStack clickedItem = event.getClickedItem();
                    
                    if (clickedItem == null) return;
                    
                    String targetName = getPlayerName(targetUuid);
                    
                    if (clickedItem.material() == Material.LAVA_BUCKET) {
                        // 추방
                        if (island.removeMember(targetUuid)) {
                            player.sendMessage(Component.text(targetName + "을(를) 섬에서 추방했습니다.")
                                    .color(TextColor.color(0xFF0000)));
                            
                            Player targetPlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(targetUuid);
                            if (targetPlayer != null) {
                                targetPlayer.sendMessage(Component.text("섬에서 추방되었습니다.")
                                        .color(TextColor.color(0xFF0000)));
                            }
                        }
                        player.closeInventory();
                    } else if (clickedItem.material() == Material.EMERALD) {
                        // 진급
                        if (island.setMemberRole(targetUuid, Island.MemberRole.MODERATOR)) {
                            player.sendMessage(Component.text(targetName + "을(를) 관리자로 승격시켰습니다.")
                                    .color(TextColor.color(0x00FF00)));
                            
                            Player targetPlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(targetUuid);
                            if (targetPlayer != null) {
                                targetPlayer.sendMessage(Component.text("섬 관리자로 승격되었습니다!")
                                        .color(TextColor.color(0x00FF00)));
                            }
                        }
                        player.closeInventory();
                    } else if (clickedItem.material() == Material.IRON_INGOT) {
                        // 강등
                        if (island.setMemberRole(targetUuid, Island.MemberRole.MEMBER)) {
                            player.sendMessage(Component.text(targetName + "을(를) 일반 멤버로 강등시켰습니다.")
                                    .color(TextColor.color(0xAAAAAA)));
                            
                            Player targetPlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(targetUuid);
                            if (targetPlayer != null) {
                                targetPlayer.sendMessage(Component.text("일반 멤버로 강등되었습니다.")
                                        .color(TextColor.color(0xAAAAAA)));
                            }
                        }
                        player.closeInventory();
                    } else if (clickedItem.material() == Material.PAPER) {
                        // 귓속말
                        player.sendMessage(Component.text("귓속말 기능은 채팅 명령어를 사용하세요: /msg " + targetName)
                                .color(TextColor.color(0xFFFF00)));
                        player.closeInventory();
                    } else if (clickedItem.material() == Material.BARRIER) {
                        // 닫기
                        player.closeInventory();
                    }
                })
                .build());
    }
    
    public void open(Player player) {
        player.openInventory(inventory);
    }
}