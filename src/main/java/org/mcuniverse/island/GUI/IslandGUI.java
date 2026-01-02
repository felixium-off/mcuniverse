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
import org.mcuniverse.island.manager.IslandManager;
import org.mcuniverse.managers.SpawnManager;

public class IslandGUI {
    private final IslandManager islandManager;
    private final Inventory inventory;
    private final EventNode<InventoryEvent> eventNode;
    
    // GUI 아이템
    private final ItemStack createIslandItem;
    private final ItemStack teleportIslandItem;
    private final ItemStack deleteIslandItem;
    private final ItemStack closeItem;
    
    public IslandGUI(IslandManager islandManager) {
        this.islandManager = islandManager;
        this.inventory = new Inventory(InventoryType.CHEST_1_ROW, Component.text("섬 관리"));
        
        // 아이템 생성
        this.createIslandItem = ItemStack.builder(Material.GRASS_BLOCK)
                .customName(Component.text("섬 생성")
                        .color(TextColor.color(0x00FF00))
                        .decorate(TextDecoration.BOLD))
                .lore(Component.text("새로운 개인 섬을 생성합니다."))
                .build();
        
        this.teleportIslandItem = ItemStack.builder(Material.ENDER_PEARL)
                .customName(Component.text("섬 이동")
                        .color(TextColor.color(0x0099FF))
                        .decorate(TextDecoration.BOLD))
                .lore(Component.text("자신의 섬으로 이동합니다."))
                .build();
        
        this.deleteIslandItem = ItemStack.builder(Material.LAVA_BUCKET)
                .customName(Component.text("섬 삭제")
                        .color(TextColor.color(0xFF0000))
                        .decorate(TextDecoration.BOLD))
                .lore(Component.text("섬을 삭제합니다. (되돌릴 수 없습니다!)"))
                .build();
        
        this.closeItem = ItemStack.builder(Material.BARRIER)
                .customName(Component.text("닫기")
                        .color(TextColor.color(0xFF0000)))
                .build();
        
        // 인벤토리에 아이템 배치
        inventory.setItemStack(2, createIslandItem);
        inventory.setItemStack(4, teleportIslandItem);
        inventory.setItemStack(6, deleteIslandItem);
        inventory.setItemStack(8, closeItem);
        
        // 이벤트 노드 생성
        this.eventNode = EventNode.type("islandGUI", EventFilter.INVENTORY,
                (event, inv) -> inv == inventory);
        
        // 이벤트 리스너 등록
        setupEventListeners();
        
        // 전역 이벤트 핸들러에 추가
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);
    }
    
    private void setupEventListeners() {
        eventNode.addListener(EventListener.builder(InventoryPreClickEvent.class)
                .filter(event -> event.getClick() instanceof Click.Left)
                .handler(event -> {
                    event.setCancelled(true);
                    Player player = event.getPlayer();
                    ItemStack clickedItem = event.getClickedItem();
                    
                    if (clickedItem == null) return;

                    // 섬 생성
                    if (clickedItem.material() == Material.GRASS_BLOCK) {
                        if (islandManager.hasIsland(player.getUuid())) {
                            player.sendMessage(Component.text("이미 섬을 가지고 있습니다!")
                                    .color(TextColor.color(0xFF0000)));
                        } else {
                            var island = islandManager.createIsland(player.getUuid());
                            player.sendMessage(Component.text("섬이 생성되었습니다!")
                                    .color(TextColor.color(0x00FF00)));
                            // 섬으로 텔레포트 - 인스턴스가 다른 경우에만 setInstance 호출
                            if (player.getInstance() != island.getInstance()) {
                                player.setInstance(island.getInstance());
                            }
                            player.teleport(island.getSpawnPoint());
                        }
                        player.closeInventory();
                    }

                    // 섬 이동
                    if (clickedItem.material() == Material.ENDER_PEARL) {
                        var island = islandManager.getIsland(player.getUuid());
                        if (island == null) {
                            player.sendMessage(Component.text("섬이 없습니다! 먼저 섬을 생성하세요.")
                                    .color(TextColor.color(0xFF0000)));
                        } else {
                            // 인스턴스가 다른 경우에만 setInstance 호출
                            if (player.getInstance() != island.getInstance()) {
                                player.setInstance(island.getInstance());
                            }
                            player.teleport(island.getSpawnPoint());
                            player.sendMessage(Component.text("섬으로 이동했습니다!")
                                    .color(TextColor.color(0x00FF00)));
                        }
                        player.closeInventory();
                    }

                    // 섬 삭제
                    if (clickedItem.material() == Material.LAVA_BUCKET) {
                        var island = islandManager.getIsland(player.getUuid());
                        if (island == null) {
                            player.sendMessage(Component.text("삭제할 섬이 없습니다.")
                                    .color(TextColor.color(0xFF0000)));
                        } else {
                            islandManager.deleteIsland(player.getUuid());
                            SpawnManager.teleportToSpawn(player);
                            player.sendMessage(Component.text("섬이 삭제되었습니다.")
                                    .color(TextColor.color(0xFF0000)));
                        }
                        player.closeInventory();
                    }
                    
                    // 닫기
                    if (clickedItem.material() == Material.BARRIER) {
                        player.closeInventory();
                    }
                })
                .build());
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public EventNode<InventoryEvent> getEventNode() {
        return eventNode;
    }
}