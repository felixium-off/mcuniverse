package org.mcuniverse.plugins.cosmetics.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class JsonCosmeticRepository implements CosmeticRepository {
    private final Path dataFolder;
    private final Gson gson = new Gson();

    public JsonCosmeticRepository(Path dataFolder) {
        this.dataFolder = dataFolder;
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(UUID uuid, Set<String> equippedIds) {
        try (Writer writer = Files.newBufferedWriter(dataFolder.resolve(uuid + ".json"))) {
            gson.toJson(equippedIds, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public Set<String> load(UUID uuid) {
        Path path = dataFolder.resolve(uuid + ".json");
        if (!Files.exists(path)) return new HashSet<>();
        try (Reader reader = Files.newBufferedReader(path)) {
            return gson.fromJson(reader, new TypeToken<Set<String>>(){}.getType());
        } catch (IOException e) { e.printStackTrace(); return new HashSet<>(); }
    }
}