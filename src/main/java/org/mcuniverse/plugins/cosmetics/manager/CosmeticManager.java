package org.mcuniverse.plugins.cosmetics.manager;

import net.minestom.server.entity.Player;
import org.mcuniverse.plugins.cosmetics.api.Cosmetic;
import org.mcuniverse.plugins.cosmetics.api.CosmeticType;
import org.mcuniverse.plugins.cosmetics.registry.CosmeticRegistry;
import org.mcuniverse.plugins.cosmetics.storage.CosmeticRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 플레이어의 치장품 장착 상태를 관리하는 매니저 클래스입니다.
 * <p>
 * 이 클래스는 플레이어별로 장착된 치장품을 {@link CosmeticType}별로 분류하여 저장하며,
 * 장착 및 해제 시 {@link Cosmetic} 인터페이스의 로직을 실행합니다.
 * </p>
 */
public class CosmeticManager {

    private final CosmeticRegistry registry;
    private final CosmeticRepository repository;

    // Thread-Safe한 상태 관리를 위해 ConcurrentHashMap 사용
    // 구조: UUID -> (CosmeticType -> Cosmetic Instance)
    private final Map<UUID, Map<CosmeticType, Cosmetic>> equippedCosmetics = new ConcurrentHashMap<>();

    public CosmeticManager(CosmeticRegistry registry, CosmeticRepository repository) {
        this.registry = registry;
        this.repository = repository;
    }

    /**
     * 플레이어에게 특정 치장품을 장착합니다.
     * 이미 해당 타입의 치장품을 장착 중이라면, 기존 것을 해제하고 교체합니다.
     *
     * @param player 대상 플레이어
     * @param cosmetic 장착할 치장품
     */
    public void equip(@NotNull Player player, @NotNull Cosmetic cosmetic) {
        Map<CosmeticType, Cosmetic> playerCosmetics = equippedCosmetics.computeIfAbsent(player.getUuid(), k -> new ConcurrentHashMap<>());
        CosmeticType type = cosmetic.getType();

        // 1. 이미 같은 타입의 치장품을 끼고 있는지 확인
        Cosmetic current = playerCosmetics.get(type);
        if (current != null) {
            // 이미 같은 ID의 치장품이라면 무시 (중복 장착 방지)
            if (current.getId().equals(cosmetic.getId())) {
                return;
            }
            // 다른 치장품이라면 기존 것 해제
            unequip(player, type);
        }

        // 2. 상태 업데이트 및 장착 로직 실행
        playerCosmetics.put(type, cosmetic);
        cosmetic.onEquip(player);
    }

    /**
     * 플레이어의 특정 타입 치장품을 해제합니다.
     *
     * @param player 대상 플레이어
     * @param type 해제할 치장품 종류
     */
    public void unequip(@NotNull Player player, @NotNull CosmeticType type) {
        Map<CosmeticType, Cosmetic> playerCosmetics = equippedCosmetics.get(player.getUuid());
        if (playerCosmetics == null) return;

        Cosmetic cosmetic = playerCosmetics.remove(type);
        if (cosmetic != null) {
            cosmetic.onUnequip(player);
        }

        // 맵이 비었으면 메모리 절약을 위해 키 제거
        if (playerCosmetics.isEmpty()) {
            equippedCosmetics.remove(player.getUuid());
        }
    }

    /**
     * 플레이어가 현재 장착 중인 특정 타입의 치장품을 가져옵니다.
     */
    public @Nullable Cosmetic getEquipped(@NotNull Player player, @NotNull CosmeticType type) {
        Map<CosmeticType, Cosmetic> playerCosmetics = equippedCosmetics.get(player.getUuid());
        return playerCosmetics != null ? playerCosmetics.get(type) : null;
    }

    /**
     * 특정 플레이어의 모든 치장품을 해제합니다. (접속 종료 시 사용)
     */
    public void unequipAll(@NotNull Player player) {
        Map<CosmeticType, Cosmetic> cosmetics = equippedCosmetics.remove(player.getUuid());
        if (cosmetics != null) {
            cosmetics.values().forEach(c -> c.onUnequip(player));
            cosmetics.clear();
        }
    }

    /**
     * 저장소에서 플레이어의 치장 데이터를 불러와 장착합니다.
     */
    public void loadData(@NotNull Player player) {
        Set<String> ids = repository.load(player.getUuid());
        for (String id : ids) {
            Cosmetic cosmetic = registry.get(id);
            if (cosmetic != null) {
                equip(player, cosmetic);
            }
        }
    }

    /**
     * 현재 장착 중인 치장 데이터를 저장소에 저장합니다.
     */
    public void saveData(@NotNull Player player) {
        Map<CosmeticType, Cosmetic> cosmetics = equippedCosmetics.get(player.getUuid());
        Set<String> ids;
        if (cosmetics != null) {
            ids = cosmetics.values().stream().map(Cosmetic::getId).collect(Collectors.toSet());
        } else {
            ids = new HashSet<>();
        }
        repository.save(player.getUuid(), ids);
    }

    /**
     * 서버 종료 시 또는 리로드 시 모든 플레이어의 치장품을 안전하게 해제합니다.
     */
    public void shutdown() {
        equippedCosmetics.forEach((uuid, map) -> {
            // 저장 후 해제 (Player 객체가 필요하므로 접속 중인 플레이어만 처리 가능)
            // 실제로는 Minestom의 ConnectionManager를 통해 Player 객체를 찾아야 함
            // 여기서는 메모리 정리만 수행
            // (서버 종료 시에는 보통 PlayerDisconnectEvent가 먼저 발생하여 저장됨)
            map.clear();
        });
        equippedCosmetics.clear();
    }
}
