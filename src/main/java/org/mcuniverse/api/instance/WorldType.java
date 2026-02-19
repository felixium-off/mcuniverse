package org.mcuniverse.api.instance;

/**
 * Enum defining world generation strategies.
 */
public enum WorldType {
    /**
     * Void world (no blocks generated).
     */
    VOID,
    
    /**
     * Flat world with grass blocks.
     */
    FLAT,
    
    /**
     * Normal world generation (placeholder for future terrain generation).
     */
    NORMAL,
    
    /**
     * World loaded from Polar format file.
     * No generation - uses existing world data.
     */
    POLAR
}
