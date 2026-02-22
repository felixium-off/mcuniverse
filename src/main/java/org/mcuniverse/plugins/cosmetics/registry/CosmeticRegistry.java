package org.mcuniverse.plugins.cosmetics.registry;

import org.jetbrains.annotations.Nullable;
import org.mcuniverse.plugins.cosmetics.api.Cosmetic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CosmeticRegistry {

    private final Map<String, Cosmetic> cosmetics = new ConcurrentHashMap<>();

    public void register(Cosmetic cosmetic) {
        cosmetics.put(cosmetic.getId(), cosmetic);
    }

    public @Nullable Cosmetic get(String id) {
        return cosmetics.get(id);
    }

    public Map<String, Cosmetic> getAll() {
        return cosmetics;
    }
}
