package org.mcuniverse.systems.entity.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.mcuniverse.systems.entity.mob.MobService;
import org.mcuniverse.systems.entity.model.BaseMob;
import org.mcuniverse.Server;

public class EntityCommand extends Command {

    public EntityCommand(MobService mobService) {
        super("mob", "mm");

        var spawnArg = ArgumentType.Literal("spawn");
        var listArg = ArgumentType.Literal("list");
        var reloadArg = ArgumentType.Literal("reload");
        var loadArg = ArgumentType.Literal("load");

        var idArg = ArgumentType.Word("id"); // 몹 ID
        var fileNameArg = ArgumentType.Word("filename"); // 파일 이름

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                String id = context.get(idArg);
                // 현재 플레이어의 위치를 가져오고, 바라보는 방향 벡터(direction)에 2를 곱해서 더함
                Pos currentPos = player.getPosition();
                Pos spawnPos = currentPos.add(currentPos.direction().mul(2));

                BaseMob mob = mobService.spawnMob(id, player.getInstance(), spawnPos);
                if (mob != null) {
                    player.sendMessage("✅ 몹이 소환되었습니다: " + id);
                } else {
                    player.sendMessage("❌ 존재하지 않는 몹 ID 입니다: " + id);
                }
            }
        }, spawnArg, idArg);

        addSyntax((sender, context) -> {
            // 로드된 몹들의 ID 목록 가져오기
            var ids = Server.MOB_MANAGER.getLoadedMobIds();
            if (ids.isEmpty()) {
                sender.sendMessage("⚠️ 로드된 몹 데이터가 없습니다.");
            } else {
                sender.sendMessage("📜 로드된 몹 ID: " + String.join(", ", ids));
            }
        }, listArg);

        addSyntax((sender, context) -> {
            Server.MOB_MANAGER.loadAllMobs();
            sender.sendMessage("🔄 모든 몹 데이터를 재로드했습니다.");
        }, reloadArg);

        addSyntax((sender, context) -> {
            String filename = context.get(fileNameArg);
            Server.MOB_MANAGER.loadMobFile(filename);
            sender.sendMessage("📎 " + filename + " 파일을 로드 시도했습니다.");
        }, loadArg, fileNameArg);
    }
}
