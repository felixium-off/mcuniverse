package org.mcuniverse.plugins.common;

import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

public interface LampExtension {
    // Lamp 빌더를 받아 설정을 추가하는 메서드
    void register(Lamp.Builder<MinestomCommandActor> builder);
}