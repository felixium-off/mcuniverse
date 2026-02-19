package org.mcuniverse.plugins.essentials.warp;

import java.util.List;

public interface WarpStrategy {
    
    void createWarp(Warp warp);
    
    void deleteWarp(String name);
    
    Warp getWarp(String name);
    
    List<String> getWarpNames();
}