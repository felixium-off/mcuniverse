package org.mcuniverse.plugins.cosmetics.storage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.mcuniverse.plugins.common.database.DatabaseManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MongoCosmeticRepository implements CosmeticRepository {

    private final MongoCollection<Document> collection;

    public MongoCosmeticRepository() {
        this.collection = DatabaseManager.getInstance().getMongoDatabase().getCollection("cosmetics");
    }

    @Override
    public void save(UUID uuid, Set<String> equippedIds) {
        // UUID를 키로 하여 equippedIds 리스트를 저장
        collection.updateOne(
                Filters.eq("uuid", uuid.toString()),
                Updates.set("equipped", equippedIds),
                new UpdateOptions().upsert(true)
        );
    }

    @Override
    public Set<String> load(UUID uuid) {
        Document doc = collection.find(Filters.eq("uuid", uuid.toString())).first();
        if (doc != null) {
            List<String> list = doc.getList("equipped", String.class);
            if (list != null) {
                return new HashSet<>(list);
            }
        }
        return new HashSet<>();
    }
}