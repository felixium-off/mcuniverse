package org.mcuniverse.plugins.user;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserStrategy {
    
    CompletableFuture<User> getUser(UUID uuid);

    CompletableFuture<User> createUser(UUID uuid, String username, String ip);
    
    CompletableFuture<Void> saveUser(User user);
}
