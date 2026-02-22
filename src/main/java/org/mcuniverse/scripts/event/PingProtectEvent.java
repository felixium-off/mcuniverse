package org.mcuniverse.scripts.event;

import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.server.ClientPingServerEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.network.player.PlayerConnection;
import org.mcuniverse.core.security.PingRateLimiter;

public class PingProtectEvent {

    private final PingRateLimiter limiter = new PingRateLimiter();

    public void register(GlobalEventHandler gh) {
        gh.addListener(ServerListPingEvent.class, event -> {
            PlayerConnection playerConnection = event.getConnection();
            if (playerConnection != null) {
                String ip = playerConnection.getRemoteAddress().toString();
                if (!limiter.isAllowed(ip)) {
                    event.setCancelled(true);
                }
            }
        });

        gh.addListener(ClientPingServerEvent.class, event -> {
            String ip = event.getConnection().getRemoteAddress().toString();
            if (!limiter.isAllowed(ip)) {
                event.setCancelled(true);
            }
        });
    }
}
