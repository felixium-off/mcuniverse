package org.mcuniverse.scripts.event;

import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.server.ClientPingServerEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.network.player.PlayerConnection;
import org.mcuniverse.core.security.PingRateLimiter;

import java.net.InetSocketAddress;

public class PingProtectEvent {

    private final PingRateLimiter limiter = new PingRateLimiter();

    public void register(GlobalEventHandler gh) {
        gh.addListener(ServerListPingEvent.class, event -> {
            // InetSocketAddress로 캐스팅하여 순수한 IP 문자열만 추출합니다.
            InetSocketAddress address = (InetSocketAddress) event.getConnection().getRemoteAddress();
            String ip = address.getAddress().getHostAddress();

            if (!limiter.isAllowed(ip)) {
                event.setCancelled(true);
            }
        });

        gh.addListener(ClientPingServerEvent.class, event -> {
            InetSocketAddress address = (InetSocketAddress) event.getConnection().getRemoteAddress();
            String ip = address.getAddress().getHostAddress();

            if (!limiter.isAllowed(ip)) {
                event.setCancelled(true);
            }
        });
    }
}
