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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class IslandListGUI {
    private final IslandManager islandManager;
    private final Inventory inventory;
    private final EventNode<InventoryEvent> eventNode;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 45; // 5줄 x 9칸 = 45개
    
    public IslandListGUI(IslandManager islandManager) {
        this.islandManager = islandManager;
        this.inventory = new Inventory(InventoryType.CHEST_6_ROW, Component.text("섬 목록"));
        
        this.eventNode = EventNode.type("islandListGUI", EventFilter.INVENTORY,
                (event, inv) -> inv == inventory);
        
        setupEventListeners();
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);
    }
    
    public void open(Player player) {
        updateInventory();
        player.openInventory(inventory);
    }
    
    private void updateInventory() {
        Collection<Island> allIslands = islandManager.getAllIslands();
        List<Island> islandList = new ArrayList<>(allIslands);
        
        int totalPages = (int) Math.ceil((double) islandList.size() / ITEMS_PER_PAGE);
        if (currentPage >= totalPages && totalPages > 0) {
            currentPage = totalPages - 1;
        }
        
        // 인벤토리 초기화 - null 대신 ItemStack.AIR 사용
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItemStack(i, ItemStack.AIR);
        }
        
        // 섬 아이템 표시
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, islandList.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Island island = islandList.get(i);
            int slot = i - startIndex;
            
            ItemStack islandItem = createIslandItem(island);
            inventory.setItemStack(slot, islandItem);
        }
        
        // 페이지네이션 버튼
        if (currentPage > 0) {
            ItemStack prevPage = ItemStack.builder(Material.ARROW)
                    .customName(Component.text("이전 페이지")
                            .color(TextColor.color(0x00FF00)))
                    .build();
            inventory.setItemStack(45, prevPage);
        }
        
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = ItemStack.builder(Material.ARROW)
                    .customName(Component.text("다음 페이지")
                            .color(TextColor.color(0x00FF00)))
                    .build();
            inventory.setItemStack(53, nextPage);
        }
        
        // 페이지 정보
        ItemStack pageInfo = ItemStack.builder(Material.BOOK)
                .customName(Component.text("페이지: " + (currentPage + 1) + " / " + Math.max(1, totalPages))
                        .color(TextColor.color(0xFFFF00)))
                .build();
        inventory.setItemStack(49, pageInfo);
        
        // 닫기 버튼
        ItemStack closeItem = ItemStack.builder(Material.BARRIER)
                .customName(Component.text("닫기")
                        .color(TextColor.color(0xFF0000)))
                .build();
        inventory.setItemStack(48, closeItem);
    }
    
    private ItemStack createIslandItem(Island island) {
        UUID ownerUuid = island.getOwnerUuid();
        String ownerName = getPlayerName(ownerUuid);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String createdDate = sdf.format(new java.util.Date(island.getCreatedAt()));
        
        return ItemStack.builder(Material.GRASS_BLOCK)
                .customName(Component.text(ownerName + "의 섬")
                        .color(TextColor.color(0x00FF00))
                        .decorate(TextDecoration.BOLD))
                .lore(
                        Component.text("주인: " + ownerName)
                                .color(TextColor.color(0xFFFFFF)),
                        Component.text("생성일: " + createdDate)
                                .color(TextColor.color(0xAAAAAA)),
                        Component.text("멤버 수: " + island.getMemberCount() + "명")
                                .color(TextColor.color(0xAAAAAA)),
                        Component.text(""),
                        Component.text("클릭하여 섬으로 이동")
                                .color(TextColor.color(0xFFFF00))
                )
                .build();
    }
    
    private String getPlayerName(UUID uuid) {
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
        if (player != null) {
            return player.getUsername();
        }
        // 오프라인 플레이어는 UUID의 일부를 표시
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
                    
                    int slot = event.getSlot();
                    
                    // 페이지네이션
                    if (slot == 45 && clickedItem.material() == Material.ARROW) {
                        // 이전 페이지
                        if (currentPage > 0) {
                            currentPage--;
                            updateInventory();
                        }
                    } else if (slot == 53 && clickedItem.material() == Material.ARROW) {
                        // 다음 페이지
                        Collection<Island> allIslands = islandManager.getAllIslands();
                        int totalPages = (int) Math.ceil((double) allIslands.size() / ITEMS_PER_PAGE);
                        if (currentPage < totalPages - 1) {
                            currentPage++;
                            updateInventory();
                        }
                    } else if (slot == 48 && clickedItem.material() == Material.BARRIER) {
                        // 닫기
                        player.closeInventory();
                    } else if (slot < 45 && clickedItem.material() == Material.GRASS_BLOCK) {
                        // 섬 클릭 - 텔레포트
                        Collection<Island> allIslands = islandManager.getAllIslands();
                        List<Island> islandList = new ArrayList<>(allIslands);
                        int startIndex = currentPage * ITEMS_PER_PAGE;
                        int islandIndex = startIndex + slot;
                        
                        if (islandIndex < islandList.size()) {
                            Island island = islandList.get(islandIndex);
                            player.setInstance(island.getInstance());
                            player.teleport(island.getSpawnPoint());
                            player.sendMessage(Component.text("섬으로 이동했습니다!")
                                    .color(TextColor.color(0x00FF00)));
                            player.closeInventory();
                        }
                    }
                })
                .build());
    }
    
    public Inventory getInventory() {
        return inventory;
    }
}