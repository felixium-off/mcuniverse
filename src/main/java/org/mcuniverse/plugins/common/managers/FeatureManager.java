package org.mcuniverse.plugins.common.managers;

import net.minestom.server.MinecraftServer;
import org.mcuniverse.plugins.common.GameFeature;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

import java.util.ArrayList;
import java.util.List;

/**
 * 모든 GameFeature의 생명주기(enable/disable)를 관리하는 클래스입니다.
 * Main 클래스의 SRP 위반을 해소하기 위해 분리되었습니다.
 */
public class FeatureManager {

    private final List<GameFeature> features = new ArrayList<>();
    private final MinecraftServer server;

    public FeatureManager(MinecraftServer server) {
        this.server = server;
    }

    /**
     * Feature를 등록합니다. 아직 활성화되지 않습니다.
     * @param feature 등록할 GameFeature
     */
    public void register(GameFeature feature) {
        features.add(feature);
    }

    /**
     * 등록된 모든 Feature를 순서대로 활성화합니다.
     * @param lamp Lamp 명령어 프레임워크 인스턴스
     */
    public void enableAll(Lamp<MinestomCommandActor> lamp) {
        for (GameFeature feature : features) {
            feature.enable(server, lamp);
        }
    }

    /**
     * 등록된 모든 Feature를 비활성화합니다.
     */
    public void disableAll() {
        for (GameFeature feature : features) {
            feature.disable(server);
        }
    }

    /**
     * 등록된 Feature 목록을 반환합니다.
     * @return Feature 목록 (읽기 전용 복사본)
     */
    public List<GameFeature> getFeatures() {
        return List.copyOf(features);
    }
}
