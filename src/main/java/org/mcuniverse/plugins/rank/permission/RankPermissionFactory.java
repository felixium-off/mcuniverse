package org.mcuniverse.plugins.rank.permission;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mcuniverse.plugins.rank.RankGroup;
import org.mcuniverse.plugins.rank.RankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public class RankPermissionFactory implements CommandPermission.Factory<MinestomCommandActor> {

    private final Logger logger = LoggerFactory.getLogger(RankPermissionFactory.class);
    private final RankService rankService;

    public RankPermissionFactory(RankService rankService) {
        this.rankService = rankService;
    }

    @Override
    @Nullable
    public CommandPermission<MinestomCommandActor> create(@NotNull AnnotationList annotations, @NotNull Lamp<MinestomCommandActor> lamp) {
        RequiresRank requiresRank = annotations.get(RequiresRank.class);
        if (requiresRank == null) return null;

        RankGroup requiredRank = RankGroup.valueOf(requiresRank.value());

        return actor -> {
            if (!(actor.sender() instanceof Player player)) {
                return true;
            }

            try {
                RankGroup currentRank = rankService.getRank(player.getUuid()).join();
                return currentRank.getLevel() >= requiredRank.getLevel();
            } catch (Exception e) {
                logger.error("[RankDB] 데이터 처리 오류", e);
                return false;
            }
        };
    }
}