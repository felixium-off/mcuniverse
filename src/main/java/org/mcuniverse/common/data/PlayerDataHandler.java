package org.mcuniverse.common.data;

import net.minestom.server.entity.Player;

public interface PlayerDataHandler {

    void onLoad(Player player);

    void onUnload(Player player);
}