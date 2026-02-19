package org.mcuniverse.plugins.essentials.warp.impl;

import org.mcuniverse.plugins.essentials.warp.Warp;
import org.mcuniverse.plugins.essentials.warp.WarpStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryWarpStrategy implements WarpStrategy {

    private final Map<String, Warp> warps = new ConcurrentHashMap<>();

    @Override
    public void createWarp(Warp warp) {
        warps.put(warp.name(), warp);
    }

    @Override
    public void deleteWarp(String name) {
        warps.remove(name);
    }

    @Override
    public Warp getWarp(String name) {
        return warps.get(name);
    }

    @Override
    public List<String> getWarpNames() {
        return new ArrayList<>(warps.keySet());
    }
}