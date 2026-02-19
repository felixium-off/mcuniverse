package org.mcuniverse.plugins.essentials;

import net.minestom.server.entity.GameMode;
import org.mcuniverse.plugins.common.LampExtension;
import revxrsal.commands.Lamp;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

import java.util.List;

public class GameModeExtension implements LampExtension {

    @Override
    public void register(Lamp.Builder<MinestomCommandActor> builder) {
        // 1. 파라미터 타입 등록
        builder.parameterTypes(types -> types.addParameterType(GameMode.class, (input, actor) -> {
            String value = input.readString(); // 스트림에서 단어 읽기
            switch (value.toLowerCase()) {
                case "0": case "s": case "survival": return GameMode.SURVIVAL;
                case "1": case "c": case "creative": return GameMode.CREATIVE;
                case "2": case "a": case "adventure": return GameMode.ADVENTURE;
                case "3": case "sp": case "spectator": return GameMode.SPECTATOR;
                default: throw new CommandErrorException("잘못된 게임모드입니다: " + value);
            }
        }));

        // 2. 자동완성(Tab Completion) 등록
        builder.suggestionProviders(providers -> providers.addProvider(GameMode.class, context ->
                List.of("survival", "creative", "adventure", "spectator")
        ));
    }
}