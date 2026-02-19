package org.mcuniverse.api.instance;

import org.mcuniverse.api.instance.exception.InstanceAlreadyExistsException;
import org.mcuniverse.api.instance.exception.InstanceLoadException;
import org.mcuniverse.api.instance.exception.InstanceNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * Manager for creating, retrieving, and managing game instances.
 * Follows Dependency Inversion Principle (DIP) for easy testing and swapping implementations.
 */
public interface InstanceProvider {
    
    /**
     * Creates a new instance with the specified name and world type.
     * 
     * @param name the unique name for the instance
     * @param type the world generation type
     * @return the created GameInstance
     * @throws InstanceAlreadyExistsException if an instance with this name already exists
     * @throws InstanceLoadException if instance creation fails
     */
    GameInstance createInstance(String name, WorldType type) 
            throws InstanceAlreadyExistsException, InstanceLoadException;
    
    /**
     * Retrieves an instance by name.
     * 
     * @param name the instance name
     * @return Optional containing the instance if found, empty otherwise
     */
    Optional<GameInstance> getInstance(String name);
    
    /**
     * Gets all currently loaded instances.
     * 
     * @return immutable list of all instances
     */
    List<GameInstance> getAllInstances();
    
    /**
     * Creates an instance from Polar world data.
     * Uses temporary file approach for PolarLoader compatibility.
     * 
     * @param name the unique name for the instance
     * @param polarData byte array containing Polar format world data
     * @return the created GameInstance
     * @throws InstanceAlreadyExistsException if an instance with this name already exists
     * @throws InstanceLoadException if instance creation fails
     */
    GameInstance createInstanceFromPolar(String name, byte[] polarData) 
            throws InstanceAlreadyExistsException, InstanceLoadException;
    
    /**
     * Unloads an instance by name.
     * 
     * @param name the instance name
     * @return true if the instance was unloaded, false if it didn't exist
     * @throws InstanceNotFoundException if the instance doesn't exist
     */
    boolean unloadInstance(String name) throws InstanceNotFoundException;
}
