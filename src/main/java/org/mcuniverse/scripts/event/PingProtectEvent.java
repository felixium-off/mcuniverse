package org.mcuniverse.scripts.event;

import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.server.ClientPingServerEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import org.mcuniverse.core.security.PingRateLimiter;

import java.net.InetSocketAddress;

public class PingProtectEvent {

    private final PingRateLimiter limiter = new PingRateLimiter();

    public void register(GlobalEventHandler eventNode) {
        eventNode.addListener(ServerListPingEvent.class, this::onServerListPing);
        eventNode.addListener(ClientPingServerEvent.class, this::onClientPing);
    }

    // 서버 목록 핑 이벤트
    private void onServerListPing(ServerListPingEvent event) {
        InetSocketAddress address = (InetSocketAddress) event.getConnection().getRemoteAddress();
        String ip = address.getAddress().getHostAddress();

        if (!limiter.isAllowed(ip)) {
            event.setCancelled(true);
        }
    }

    // 클라이언트 핑 이벤트
    private void onClientPing(ClientPingServerEvent event) {
        InetSocketAddress address = (InetSocketAddress) event.getConnection().getRemoteAddress();
        String ip = address.getAddress().getHostAddress();

        if (!limiter.isAllowed(ip)) {
            event.setCancelled(true);
        }
    }
}
