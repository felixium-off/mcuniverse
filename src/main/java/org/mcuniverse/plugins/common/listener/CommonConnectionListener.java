package org.mcuniverse.plugins.common.listener;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import org.mcuniverse.plugins.common.data.PlayerDataHandler;

import java.util.ArrayList;
import java.util.List;

public class CommonConnectionListener {

    private final List<PlayerDataHandler> handlers = new ArrayList<>();

    public CommonConnectionListener(EventNode<Event> eventNode) {
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, this::onJoin);
        eventNode.addListener(PlayerDisconnectEvent.class, this::onQuit);
    }

    public void addHandler(PlayerDataHandler handler) {
        handlers.add(handler);
    }

    private void onJoin(AsyncPlayerConfigurationEvent event) {
        for (PlayerDataHandler handler : handlers) {
            handler.onLoad(event.getPlayer());
        }
    }

    private void onQuit(PlayerDisconnectEvent event) {
        for (PlayerDataHandler handler : handlers) {
            handler.onUnload(event.getPlayer());
        }
    }
}
