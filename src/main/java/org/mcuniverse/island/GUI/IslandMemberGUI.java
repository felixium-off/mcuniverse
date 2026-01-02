package org.mcuniverse.island.GUI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

import java.util.List;
import java.util.UUID;

public class IslandMemberGUI {
    private final IslandManager islandManager;
    private final Island island;
    private final Inventory inventory;
    private final EventNode<InventoryEvent> eventNode;
    
    public IslandMemberGUI(IslandManager islandManager, Island island) {
        this.islandManager = islandManager;
        this.island = island;
        this.inventory = new Inventory(InventoryType.CHEST_6_ROW, 
                Component.text("섬 멤버 관리"));
        
        this.eventNode = EventNode.type("islandMemberGUI", EventFilter.INVENTORY,
                (event, inv) -> inv == inventory);
        
        setupEventListeners();
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);
    }
    
    public void open(Player player) {
        updateInventory();
        player.openInventory(inventory);
    }
    
    private void updateInventory() {
        // 인벤토리 초기화 - null 대신 ItemStack.AIR 사용
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItemStack(i, ItemStack.AIR);
        }
        
        List<UUID> members = island.getMembers();
        
        // 주인 표시
        UUID ownerUuid = island.getOwnerUuid();
        ItemStack ownerItem = createMemberItem(ownerUuid, Island.MemberRole.OWNER, true);
        inventory.setItemStack(4, ownerItem);
        
        // 멤버 표시 (슬롯 9부터 시작)
        int slot = 9;
        for (UUID memberUuid : members) {
            if (memberUuid.equals(ownerUuid)) continue; // 주인은 이미 표시됨
            
            Island.MemberRole role = island.getMemberRole(memberUuid);
            ItemStack memberItem = createMemberItem(memberUuid, role, false);
            inventory.setItemStack(slot, memberItem);
            slot++;
            
            if (slot >= 45) break; // 인벤토리 끝
        }
        
        // 닫기 버튼
        ItemStack closeItem = ItemStack.builder(Material.BARRIER)
                .customName(Component.text("닫기")
                        .color(TextColor.color(0xFF0000)))
                .build();
        inventory.setItemStack(49, closeItem);
    }
    
    private ItemStack createMemberItem(UUID memberUuid, Island.MemberRole role, boolean isOwner) {
        String playerName = getPlayerName(memberUuid);
        String roleName = isOwner ? "주인" : 
                         (role == Island.MemberRole.MODERATOR ? "관리자" : "멤버");
        TextColor roleColor = isOwner ? TextColor.color(0xFFD700) :
                             (role == Island.MemberRole.MODERATOR ? TextColor.color(0x00FF00) : 
                              TextColor.color(0xFFFFFF));
        
        boolean isOnline = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(memberUuid) != null;
        
        return ItemStack.builder(isOwner ? Material.GOLD_BLOCK : 
                                (role == Island.MemberRole.MODERATOR ? Material.EMERALD_BLOCK : 
                                 Material.PLAYER_HEAD))
                .customName(Component.text(playerName)
                        .color(roleColor)
                        .decorate(TextDecoration.BOLD))
                .lore(
                        Component.text("역할: " + roleName)
                                .color(roleColor),
                        Component.text("상태: " + (isOnline ? "온라인" : "오프라인"))
                                .color(isOnline ? TextColor.color(0x00FF00) : TextColor.color(0xAAAAAA)),
                        Component.text(""),
                        Component.text("좌클릭: 귓속말")
                                .color(TextColor.color(0xFFFF00)),
                        Component.text("우클릭: 메뉴 열기")
                                .color(TextColor.color(0xFFFF00))
                )
                .build();
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
                .handler(event -> {
                    event.setCancelled(true);
                    Player player = event.getPlayer();
                    ItemStack clickedItem = event.getClickedItem();
                    
                    if (clickedItem == null) return;
                    
                    int slot = event.getSlot();
                    
                    // 닫기
                    if (slot == 49 && clickedItem.material() == Material.BARRIER) {
                        player.closeInventory();
                        return;
                    }
                    
                    // 멤버 아이템 클릭
                    if (slot >= 4 && slot < 45) {
                        List<UUID> members = island.getMembers();
                        UUID clickedMemberUuid = null;
                        
                        // 슬롯에서 멤버 UUID 찾기
                        if (slot == 4) {
                            clickedMemberUuid = island.getOwnerUuid();
                        } else if (slot >= 9) {
                            int memberIndex = slot - 9;
                            List<UUID> nonOwnerMembers = members.stream()
                                    .filter(uuid -> !uuid.equals(island.getOwnerUuid()))
                                    .toList();
                            if (memberIndex < nonOwnerMembers.size()) {
                                clickedMemberUuid = nonOwnerMembers.get(memberIndex);
                            }
                        }
                        
                        if (clickedMemberUuid == null) return;
                        
                        // 주인만 멤버 관리 가능
                        if (!island.isOwner(player.getUuid())) {
                            player.sendMessage(Component.text("섬 주인만 멤버를 관리할 수 있습니다!")
                                    .color(TextColor.color(0xFF0000)));
                            return;
                        }
                        
                        // 자신은 관리 불가
                        if (clickedMemberUuid.equals(player.getUuid())) {
                            player.sendMessage(Component.text("자신은 관리할 수 없습니다!")
                                    .color(TextColor.color(0xFF0000)));
                            return;
                        }
                        
                        if (event.getClick() instanceof Click.Left) {
                            // 좌클릭: 귓속말
                            openWhisperGUI(player, clickedMemberUuid);
                        } else if (event.getClick() instanceof Click.Right) {
                            // 우클릭: 멤버 관리 메뉴
                            openMemberManageGUI(player, clickedMemberUuid);
                        }
                    }
                })
                .build());
    }
    
    private void openWhisperGUI(Player player, UUID targetUuid) {
        // 간단한 귓속말 - 채팅으로 대체 가능
        String targetName = getPlayerName(targetUuid);
        player.sendMessage(Component.text("귓속말 기능은 채팅 명령어를 사용하세요: /msg " + targetName)
                .color(TextColor.color(0xFFFF00)));
    }
    
    private void openMemberManageGUI(Player player, UUID targetUuid) {
        // 멤버 관리 GUI 열기
        IslandMemberManageGUI manageGUI = new IslandMemberManageGUI(islandManager, island, targetUuid);
        manageGUI.open(player);
    }
    
    public Inventory getInventory() {
        return inventory;
    }
}