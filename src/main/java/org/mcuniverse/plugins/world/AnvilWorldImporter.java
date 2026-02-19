package org.mcuniverse.plugins.world;

import net.hollowcube.polar.AnvilPolar;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility for converting Anvil format worlds to Polar format.
 * Reads from the worlds/ directory and converts to Polar bytes.
 */
public class AnvilWorldImporter {
    
    private static final Path WORLDS_DIR = Path.of("worlds");
    
    /**
     * Import an Anvil world and convert it to Polar format.
     * 
     * @param worldName Name of the world folder in worlds/
     * @return Polar format byte array
     * @throws Exception if world doesn't exist or conversion fails
     */
    public static byte[] importAnvilWorld(@NotNull String worldName) throws Exception {
        Path anvilWorldPath = WORLDS_DIR.resolve(worldName);
        
        // Verify world exists
        if (!Files.exists(anvilWorldPath)) {
            throw new IllegalArgumentException("World not found: worlds/" + worldName);
        }
        
        if (!Files.isDirectory(anvilWorldPath)) {
            throw new IllegalArgumentException("Not a directory: worlds/" + worldName);
        }
        
        // Convert Anvil → Polar
        PolarWorld polarWorld = AnvilPolar.anvilToPolar(anvilWorldPath);
        
        // Serialize to bytes
        return PolarWriter.write(polarWorld);
    }
    
    /**
     * Check if a world exists in the worlds/ directory.
     */
    public static boolean worldExists(@NotNull String worldName) {
        Path worldPath = WORLDS_DIR.resolve(worldName);
        return Files.exists(worldPath) && Files.isDirectory(worldPath);
    }
    
    /**
     * List all available Anvil worlds in the worlds/ directory.
     */
    public static String[] listAvailableWorlds() throws Exception {
        if (!Files.exists(WORLDS_DIR)) {
            Files.createDirectories(WORLDS_DIR);
            return new String[0];
        }
        
        return Files.list(WORLDS_DIR)
            .filter(Files::isDirectory)
            .map(Path::getFileName)
            .map(Path::toString)
            .toArray(String[]::new);
    }
}
