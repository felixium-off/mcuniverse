package org.mcuniverse.systems.entity.model;

import org.mcuniverse.systems.entity.data.PlayerDTO;

import net.minestom.server.entity.Player;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;

public class CustomPlayer extends Player {

    private PlayerDTO playerDTO;

    public CustomPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
    }

    public void injectPlayerDTO(PlayerDTO dto) {
        this.playerDTO = dto;
    }

    public PlayerDTO getPlayerDTO() {
        return playerDTO;
    }

    public PlayerDTO.PlayerStats getStats() {
        if (playerDTO == null || playerDTO.getStats() == null) {
            return null;
        }
        return playerDTO.getStats();
    }

    public PlayerDTO.PlayerEquipment getEquipment() {
        if (playerDTO == null || playerDTO.getEquipment() == null) {
            return null;
        }
        return playerDTO.getEquipment();
    }
}
