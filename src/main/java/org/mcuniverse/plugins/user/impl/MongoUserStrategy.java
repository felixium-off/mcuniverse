package org.mcuniverse.plugins.user.impl;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.mcuniverse.plugins.common.database.AbstractMongoRedisStrategy;
import org.mcuniverse.plugins.user.User;
import org.mcuniverse.plugins.user.UserImpl;
import org.mcuniverse.plugins.user.UserStrategy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoUserStrategy extends AbstractMongoRedisStrategy implements UserStrategy {

    public MongoUserStrategy() {
        super("users", "mc:user:");
    }

    @Override
    public CompletableFuture<User> getUser(UUID uuid) {
        String key = getKey(uuid);

        // 1. Redis Cache Hit 확인
        Map<String, String> cached = getRedis().hgetall(key);
        if (!cached.isEmpty()) {
            getRedis().persist(key); // 생존 신고
            return CompletableFuture.completedFuture(mapToUser(uuid, cached));
        }

        // 2. Cache Miss -> DB Load
        return supplyAsync(() -> {
            Document doc = collection.find(Filters.eq("uuid", uuid.toString())).first();
            if (doc == null) return null;

            User user = docToUser(doc);
            
            // Redis Caching
            getRedis().hmset(key, userToMap(user));
            getRedis().persist(key);
            
            return user;
        });
    }

    @Override
    public CompletableFuture<User> createUser(UUID uuid, String username, String ip) {
        return supplyAsync(() -> {
            User user = UserImpl.builder()
                    .uuid(uuid)
                    .username(username)
                    .createIp(ip)
                    .lastUpdateIp(ip)
                    .build();

            // DB Insert
            Document doc = userToDoc(user);
            collection.insertOne(doc);

            // Redis Cache
            String key = getKey(uuid);
            getRedis().hmset(key, userToMap(user));
            
            return user;
        });
    }

    @Override
    public CompletableFuture<Void> saveUser(User user) {
        return runAsync(() -> {
            // Redis Update
            String key = getKey(user.getUuid());
            getRedis().hmset(key, userToMap(user));
            getRedis().persist(key);

            // DB Update (비동기)
            getExecutor().execute(() -> {
                collection.replaceOne(Filters.eq("uuid", user.getUuid().toString()), userToDoc(user));
            });
        });
    }

    // --- Mappers ---

    private Map<String, String> userToMap(User user) {
        Map<String, String> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("createIp", user.getCreateIp());
        map.put("lastUpdateIp", user.getLastUpdateIp());
        map.put("dateCreated", String.valueOf(user.getDateCreated().getTime()));
        map.put("lastUpdated", String.valueOf(user.getLastUpdated().getTime()));
        map.put("enabled", String.valueOf(user.isEnabled()));
        
        if (user.getLockedDate() != null) map.put("locked", String.valueOf(user.getLockedDate().getTime()));
        if (user.getRankExpiredDate() != null) map.put("rankExpired", String.valueOf(user.getRankExpiredDate().getTime()));
        
        return map;
    }

    private User mapToUser(UUID uuid, Map<String, String> map) {
        UserImpl.UserImplBuilder builder = UserImpl.builder()
                .uuid(uuid)
                .username(map.get("username"))
                .createIp(map.get("createIp"))
                .lastUpdateIp(map.get("lastUpdateIp"))
                .dateCreated(new Date(Long.parseLong(map.get("dateCreated"))))
                .lastUpdated(new Date(Long.parseLong(map.get("lastUpdated"))))
                .enabled(Boolean.parseBoolean(map.get("enabled")));

        if (map.containsKey("locked")) builder.locked(new Date(Long.parseLong(map.get("locked"))));
        if (map.containsKey("rankExpired")) builder.rankExpired(new Date(Long.parseLong(map.get("rankExpired"))));

        return builder.build();
    }

    private Document userToDoc(User user) {
        return new Document("uuid", user.getUuid().toString())
                .append("username", user.getUsername())
                .append("createIp", user.getCreateIp())
                .append("lastUpdateIp", user.getLastUpdateIp())
                .append("dateCreated", user.getDateCreated())
                .append("lastUpdated", user.getLastUpdated())
                .append("enabled", user.isEnabled())
                .append("locked", user.getLockedDate())
                .append("rankExpired", user.getRankExpiredDate());
    }

    private User docToUser(Document doc) {
        UserImpl.UserImplBuilder builder = UserImpl.builder()
                .uuid(UUID.fromString(doc.getString("uuid")))
                .username(doc.getString("username"))
                .createIp(doc.getString("createIp"))
                .lastUpdateIp(doc.getString("lastUpdateIp"))
                .dateCreated(doc.getDate("dateCreated"))
                .lastUpdated(doc.getDate("lastUpdated"))
                .enabled(doc.getBoolean("enabled", true));

        if (doc.getDate("locked") != null) builder.locked(doc.getDate("locked"));
        if (doc.getDate("rankExpired") != null) builder.rankExpired(doc.getDate("rankExpired"));

        return builder.build();
    }
}