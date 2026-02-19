package org.mcuniverse.plugins.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    private static final Properties properties = new Properties();
    private static final Path CONFIG_PATH = Path.of("config.yml");

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            createDefault();
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            properties.load(reader);
        } catch (IOException e) {
            logger.error("Failed to load config.yml", e);
        }
    }

    private static void createDefault() {
        String content = """
                # [Storage Configuration]
                # Economy Options: MEMORY, MONGODB
                # Cosmetics Options: JSON, MONGODB
                
                storage.economy.type: MEMORY
                storage.cosmetics.type: JSON
                
                # MongoDB Settings
                mongodb.uri: mongodb://localhost:27017
                mongodb.database: mcuniverse
                """;
        try {
            Files.writeString(CONFIG_PATH, content);
        } catch (IOException e) {
            logger.error("Failed to create default config.yml", e);
        }
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}