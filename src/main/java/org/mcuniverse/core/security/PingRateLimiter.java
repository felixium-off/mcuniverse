package org.mcuniverse.core.security;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class PingRateLimiter {

    private final Cache<String, Integer> pingCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    private static final int MAX_PINGS_PER_SECOND = 3;

    public boolean isAllowed(String ip) {
        int currentPings = pingCache.asMap().compute(ip, (key, count) -> count == null ? 1 : count + 1);
        return currentPings <= MAX_PINGS_PER_SECOND;
    }
}