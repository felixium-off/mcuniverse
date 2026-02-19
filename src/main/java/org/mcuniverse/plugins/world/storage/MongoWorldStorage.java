package org.mcuniverse.plugins.world.storage;

import com.github.luben.zstd.Zstd;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.mcuniverse.api.world.WorldStorage;
import org.mcuniverse.api.world.exception.CompressionException;
import org.mcuniverse.api.world.exception.WorldStorageException;

import java.util.ConcurrentModificationException;
import java.util.Date;

import static com.mongodb.client.model.Filters.eq;

/**
 * MongoDB-based world storage with Zstd compression.
 * Used for storing dynamic player island data with optimistic locking.
 */
public class MongoWorldStorage implements WorldStorage {
    
    private final MongoCollection<Document> collection;
    
    /**
     * Create MongoDB storage with connection URI and database/collection names.
     * 
     * @param mongoUri MongoDB connection URI
     * @param databaseName Database name
     * @param collectionName Collection name (e.g., "islands")
     */
    public MongoWorldStorage(String mongoUri, String databaseName, String collectionName) {
        MongoClient mongoClient = MongoClients.create(mongoUri);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        this.collection = database.getCollection(collectionName);
    }
    
    /**
     * Create MongoDB storage with existing MongoCollection.
     */
    public MongoWorldStorage(MongoCollection<Document> collection) {
        this.collection = collection;
    }
    
    @Override
    public void saveWorld(@NotNull String worldId, byte[] polarData) throws WorldStorageException {
        saveWorldWithOptimisticLock(worldId, polarData);
    }
    
    /**
     * Save world data with optimistic locking to prevent concurrent modification issues.
     */
    private void saveWorldWithOptimisticLock(String worldId, byte[] polarData) throws WorldStorageException {
        // 1. Get current version
        Document current = collection.find(eq("_id", worldId)).first();
        int currentVersion = (current != null) ? current.getInteger("version", 0) : 0;
        
        // 2. Compress data
        byte[] compressed;
        try {
            compressed = Zstd.compress(polarData);
        } catch (Exception e) {
            throw new CompressionException("Failed to compress world data for: " + worldId, e);
        }
        
        // 3. Prepare document
        Document worldDoc = new Document()
            .append("_id", worldId)
            .append("type", "island")
            .append("data", compressed)
            .append("originalSize", polarData.length)
            .append("compressedSize", compressed.length)
            .append("lastModified", new Date());
        
        // 4. Update with version check
        Document filter = new Document("_id", worldId)
            .append("version", currentVersion);
        
        Document update = new Document("$set", worldDoc)
            .append("$inc", new Document("version", 1));
        
        UpdateResult result = collection.updateOne(filter, update, 
            new UpdateOptions().upsert(true));
        
        // 5. Detect conflicts
        if (result.getModifiedCount() == 0 && current != null) {
            throw new ConcurrentModificationException(
                "Island was modified elsewhere. Please try again: " + worldId
            );
        }
    }
    
    @Override
    public byte[] loadWorld(@NotNull String worldId) throws WorldStorageException {
        Document doc = collection.find(eq("_id", worldId)).first();
        
        if (doc == null) {
            throw new WorldStorageException("World not found: " + worldId);
        }
        
        byte[] compressed = (byte[]) doc.get("data");
        if (compressed == null) {
            throw new WorldStorageException("World data is null: " + worldId);
        }
        
        // Decompress
        try {
            int originalSize = doc.getInteger("originalSize", 0);
            return Zstd.decompress(compressed, originalSize);
        } catch (Exception e) {
            throw new CompressionException("Failed to decompress world data for: " + worldId, e);
        }
    }
    
    @Override
    public boolean exists(@NotNull String worldId) {
        return collection.find(eq("_id", worldId)).first() != null;
    }
    
    @Override
    public void deleteWorld(@NotNull String worldId) throws WorldStorageException {
        collection.deleteOne(eq("_id", worldId));
    }
}
