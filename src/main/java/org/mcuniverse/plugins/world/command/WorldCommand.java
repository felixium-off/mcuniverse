package org.mcuniverse.plugins.world.command;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.mcuniverse.api.instance.GameInstance;
import org.mcuniverse.api.instance.InstanceProvider;
import org.mcuniverse.api.world.WorldStorage;
import org.mcuniverse.plugins.common.managers.SpawnManager;
import org.mcuniverse.plugins.rank.permission.RequiresRank;
import org.mcuniverse.plugins.world.AnvilWorldImporter;
import org.mcuniverse.plugins.world.WorldRegistry;
import revxrsal.commands.annotation.*;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

/**
 * Admin commands for managing worlds.
 * Provides load, reload, import, teleport, and list functionality.
 */
@Command("world")
@Description("월드 관리 명령어")
public class WorldCommand {
    
    private final WorldRegistry registry;
    private final InstanceProvider instanceProvider;
    private final WorldStorage fileStorage;
    
    public WorldCommand(@NotNull WorldRegistry registry,
                        @NotNull InstanceProvider instanceProvider,
                        @NotNull WorldStorage fileStorage) {
        this.registry = registry;
        this.instanceProvider = instanceProvider;
        this.fileStorage = fileStorage;
    }
    
    /**
     * Load an Anvil world from worlds/ directory and create an instance.
     */
    @Subcommand("load")
    @RequiresRank("ADMIN")
    @Description("Anvil 월드를 로드합니다")
    public void load(@NotNull MinestomCommandActor actor, @Named("world-name") String worldName) {
        actor.reply("§e월드 로딩 중: " + worldName + "...");
        
        // Check if already loaded
        if (registry.isLoaded(worldName)) {
            actor.error("§c이미 로드된 월드입니다: " + worldName);
            return;
        }
        
        try {
            // 1. Import Anvil → Polar
            byte[] polarData = AnvilWorldImporter.importAnvilWorld(worldName);
            
            // 2. Create instance from Polar data
            GameInstance instance = instanceProvider.createInstanceFromPolar(worldName, polarData);
            
            // 3. Register in WorldRegistry
            registry.register(worldName, instance);
            
            actor.reply("§a✓ 월드 로드 완료: " + worldName);
            
            // Add to autoload configuration
            try {
                org.mcuniverse.plugins.world.config.WorldConfig config = 
                    org.mcuniverse.plugins.world.config.WorldConfig.load();
                if (!config.hasWorld(worldName)) {
                    config.addWorld(worldName, "anvil", null);
                    config.save();
                    actor.reply("§7자동 로드 목록에 추가됨");
                }
            } catch (Exception saveEx) {
                actor.reply("§c설정 저장 실패: " + saveEx.getMessage());
            }
        } catch (IllegalArgumentException e) {
            actor.error("§c" + e.getMessage());
        } catch (Exception e) {
            actor.error("§c월드 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Reload a world (unload and load again).
     */
    @Subcommand("reload")
    @RequiresRank("ADMIN")
    @Description("월드를 리로드합니다")
    public void reload(@NotNull MinestomCommandActor actor, @Named("world-name") String worldName) {
        actor.reply("§e월드 리로딩 중: " + worldName + "...");
        
        try {
            // 1. Unload if exists
            if (registry.isLoaded(worldName)) {
                instanceProvider.unloadInstance(worldName);
                registry.unload(worldName);
                actor.reply("§7기존 인스턴스 언로드 완료");
            }
            
            // 2. Load again
            load(actor, worldName);
        } catch (Exception e) {
            actor.error("§c월드 리로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Import Anvil world to Polar format and save to templates/ directory.
     */
    @Subcommand("import")
    @RequiresRank("ADMIN")
    @Description("Anvil 월드를 Polar로 변환하여 저장합니다")
    public void importWorld(@NotNull MinestomCommandActor actor,
                           @Named("world-name") String worldName,
                           @Named("save-name") String saveName) {
        actor.reply("§e월드 변환 중: " + worldName + " → " + saveName + ".polar...");
        
        try {
            // Convert Anvil → Polar
            byte[] polarData = AnvilWorldImporter.importAnvilWorld(worldName);
            
            // Save to templates/ directory
            fileStorage.saveWorld("templates/" + saveName, polarData);
            
            actor.reply("§a✓ 변환 완료: worlds_polar/templates/" + saveName + ".polar");
            actor.reply("§7크기: " + (polarData.length / 1024) + " KB");
        } catch (Exception e) {
            actor.error("§c변환 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Teleport to a loaded world.
     * If spawn position is saved, teleports to that position.
     */
    @Subcommand("tp")
    @RequiresRank("ADMIN")
    @Description("특정 월드로 텔레포트합니다")
    public void teleport(@NotNull Player player, @Named("world-name") String worldName) {
        registry.get(worldName).ifPresentOrElse(
            instance -> {
                // Get saved spawn position for this world
                var savedSpawn = SpawnManager.getRegisteredSpawn(worldName);
                
                if (savedSpawn != null) {
                    // Teleport to saved spawn position
                    player.setInstance(instance.getContainer(), savedSpawn);
                    player.sendMessage("§a✓ " + worldName + "의 스폰으로 이동했습니다");
                    player.sendMessage("§7좌표: §f" + 
                        String.format("%.1f, %.1f, %.1f", savedSpawn.x(), savedSpawn.y(), savedSpawn.z()));
                } else {
                    // Just move to the world (default spawn)
                    player.setInstance(instance.getContainer());
                    player.sendMessage("§a✓ " + worldName + "으로 이동했습니다");
                    player.sendMessage("§e스폰이 설정되지 않음. /world setspawn으로 설정하세요");
                }
            },
            () -> player.sendMessage("§c월드를 찾을 수 없습니다: " + worldName + "\n§7/world load <name> 으로 먼저 로드하세요")
        );
    }
    
    /**
     * List all loaded worlds.
     */
    @Subcommand("list")
    @RequiresRank("ADMIN")
    @Description("로드된 월드 목록을 확인합니다")
    public void list(@NotNull MinestomCommandActor actor) {
        var worlds = registry.listWorlds();
        
        if (worlds.isEmpty()) {
            actor.reply("§e로드된 월드가 없습니다");
            actor.reply("§7/world load <name> 으로 월드를 로드하세요");
            return;
        }
        
        actor.reply("§6=== 로드된 월드 (" + worlds.size() + ") ===");
        worlds.forEach(world -> actor.reply("§7- §f" + world));
    }
    
    /**
     * List available Anvil worlds in worlds/ directory.
     */
    @Subcommand("available")
    @RequiresRank("ADMIN")
    @Description("worlds/ 폴더의 사용 가능한 월드 목록")
    public void available(@NotNull MinestomCommandActor actor) {
        try {
            String[] worlds = AnvilWorldImporter.listAvailableWorlds();
            
            if (worlds.length == 0) {
                actor.reply("§eworlds/ 폴더에 월드가 없습니다");
                return;
            }
            
            actor.reply("§6=== 사용 가능한 월드 (" + worlds.length + ") ===");
            for (String world : worlds) {
                boolean loaded = registry.isLoaded(world);
                String status = loaded ? "§a✓" : "§7○";
                actor.reply(status + " §f" + world);
            }
        } catch (Exception e) {
            actor.error("§c목록 가져오기 실패: " + e.getMessage());
        }
    }

    /**
     * Set the spawn point for a world to the player's current position.
     * Players will spawn here when joining the server.
     */
    @Subcommand("setspawn")
    @RequiresRank("ADMIN")
    @Description("현재 위치를 월드 스폰으로 설정합니다")
    public void setSpawn(@NotNull Player player, @Named("world-name") String worldName) {
        registry.get(worldName).ifPresentOrElse(
            instance -> {
                // Get player's current position
                var currentPos = player.getPosition();
                
                // Set as the instance spawn point
                SpawnManager.setSpawn(
                    instance.getContainer(),
                    currentPos
                );
                
                // Also set as player's respawn point
                player.setRespawnPoint(currentPos);
                
                player.sendMessage("§a✓ 스폰 위치 설정 완료!");
                player.sendMessage("§7월드: §f" + worldName);
                player.sendMessage("§7좌표: §f" + 
                    String.format("%.1f, %.1f, %.1f", currentPos.x(), currentPos.y(), currentPos.z()));
                
                // Save to configuration
                try {
                    org.mcuniverse.plugins.world.config.WorldConfig config = 
                        org.mcuniverse.plugins.world.config.WorldConfig.load();
                    config.updateSpawn(worldName, currentPos);
                    config.save();
                    
                    player.sendMessage("§a✓ 설정 파일에 저장 완료!");
                } catch (Exception saveEx) {
                    player.sendMessage("§c설정 저장 실패: " + saveEx.getMessage());
                }
            },
            () -> player.sendMessage("§c월드를 찾을 수 없습니다: " + worldName + "\n§7/world load <name> 으로 먼저 로드하세요")
        );
    }
}
