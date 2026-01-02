package org.mcuniverse.island.generate;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;

public class IslandGenerator {
    
    /**
     * 섬을 생성하는 Generator 함수입니다.
     * Minestom의 setGenerator는 GenerationUnit을 받는 함수형 인터페이스를 사용합니다.
     */
    public static void generateIsland(GenerationUnit unit) {
        // GenerationUnit의 절대 좌표 범위 가져오기
        Point unitStart = unit.absoluteStart();
        Point unitEnd = unit.absoluteEnd();
        
        // 청크 좌표 가져오기
        int chunkX = unitStart.chunkX();
        int chunkZ = unitStart.chunkZ();
        
        // 중앙 청크(0, 0)에만 섬 생성
        if (chunkX == 0 && chunkZ == 0) {
            int islandSize = 7; // 섬 반경
            int baseY = 60;
            
            // 섬 바닥 생성 (원형)
            // 절대 좌표를 사용하여 섬 생성
            for (int worldX = -islandSize; worldX <= islandSize; worldX++) {
                for (int worldZ = -islandSize; worldZ <= islandSize; worldZ++) {
                    double distance = Math.sqrt(worldX * worldX + worldZ * worldZ);
                    if (distance <= islandSize) {
                        // 현재 유닛의 범위 내에 있는지 확인
                        if (worldX >= unitStart.blockX() && worldX < unitEnd.blockX() &&
                            worldZ >= unitStart.blockZ() && worldZ < unitEnd.blockZ()) {
                            
                            // 섬 표면 (잔디 블록)
                            unit.modifier().setBlock(worldX, baseY, worldZ, Block.GRASS_BLOCK);
                            
                            // 섬 아래 블록 (흙)
                            for (int y = baseY - 1; y >= baseY - 3; y--) {
                                if (y >= unitStart.blockY() && y < unitEnd.blockY()) {
                                    unit.modifier().setBlock(worldX, y, worldZ, Block.DIRT);
                                }
                            }
                            
                            // 섬 맨 아래 (돌)
                            if (baseY - 3 >= unitStart.blockY() && baseY - 3 < unitEnd.blockY()) {
                                unit.modifier().setBlock(worldX, baseY - 3, worldZ, Block.STONE);
                            }
                        }
                    }
                }
            }
            
            // 중앙에 나무 생성 (범위 확인)
            if (0 >= unitStart.blockX() && 0 < unitEnd.blockX() &&
                0 >= unitStart.blockZ() && 0 < unitEnd.blockZ()) {
                
                if (baseY + 1 >= unitStart.blockY() && baseY + 1 < unitEnd.blockY()) {
                    unit.modifier().setBlock(0, baseY + 1, 0, Block.OAK_LOG);
                }
                if (baseY + 2 >= unitStart.blockY() && baseY + 2 < unitEnd.blockY()) {
                    unit.modifier().setBlock(0, baseY + 2, 0, Block.OAK_LOG);
                }
                if (baseY + 3 >= unitStart.blockY() && baseY + 3 < unitEnd.blockY()) {
                    unit.modifier().setBlock(0, baseY + 3, 0, Block.OAK_LOG);
                }
                
                // 나무 잎
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x != 0 || z != 0) {
                            int leafX = x;
                            int leafZ = z;
                            if (leafX >= unitStart.blockX() && leafX < unitEnd.blockX() &&
                                leafZ >= unitStart.blockZ() && leafZ < unitEnd.blockZ() &&
                                baseY + 4 >= unitStart.blockY() && baseY + 4 < unitEnd.blockY()) {
                                unit.modifier().setBlock(leafX, baseY + 4, leafZ, Block.OAK_LEAVES);
                            }
                        }
                    }
                }
            }
        }
    }
}