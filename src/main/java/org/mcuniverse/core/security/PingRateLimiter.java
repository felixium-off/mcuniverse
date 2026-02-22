package org.mcuniverse.core.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PingRateLimiter {

    private final ConcurrentHashMap<String, AtomicInteger> pingMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> windowStart = new ConcurrentHashMap<>();

    private static final int MAX_PINGS_PER_SECOND = 3;

    public boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        windowStart.putIfAbsent(ip, now);

        if (now - windowStart.get(ip) > 1000) {
            pingMap.put(ip, new AtomicInteger(0));
            windowStart.put(ip, now);
        }

        pingMap.putIfAbsent(ip, new AtomicInteger(0));
        return pingMap.get(ip).incrementAndGet() <= MAX_PINGS_PER_SECOND;
    }
}