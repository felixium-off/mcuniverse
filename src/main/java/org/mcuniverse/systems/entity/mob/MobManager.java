package org.mcuniverse.systems.entity.mob;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.mcuniverse.systems.entity.data.MobDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MobManager {

    private static final Logger log = LoggerFactory.getLogger(MobManager.class);

    private final Map<String, MobDTO> mobs = new ConcurrentHashMap<>();

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public void loadAllMobs() {
        mobs.clear();
        Path mobDir = Paths.get("systems/mobs");
        if (!Files.exists(mobDir)) {
            try {
                Files.createDirectories(mobDir);
                log.info("[MobManager] mobs 폴더 생성");
                return;
            } catch (IOException e) {
                log.error("[MobManager] mobs 폴더 생성 실패", e);
                return;
            }
        }

        try (Stream<Path> paths = Files.walk(mobDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(this::loadJsonMob);

            log.info("[MobManager] {} mobs 로드", mobs.size());
        } catch (IOException e) {
            log.error("[MobManager] mobs 폴더 로드 실패", e);
        }
    }

    private void loadJsonMob(Path path) {
        try (FileReader reader = new FileReader(path.toFile())) {
            MobDTO mobJson = gson.fromJson(reader, MobDTO.class);

            if (mobJson != null && mobJson.getId() != null) {
                mobs.put(mobJson.getId(), mobJson);
            } else {
                log.warn("[MobManager] 유효하지 않은 몬스터 데이터: {}", path);
            }
        } catch (IOException e) {
            log.error("[MobManager] 파일 파싱 실패: {}", path, e);
        }
    }

    public MobDTO getMob(String id) {
        return mobs.get(id);
    }

    public Set<String> getLoadedMobIds() {
        return mobs.keySet();
    }

    public void loadMobFile(String filename) {
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        Path filePath = Paths.get("systems/mobs", filename);
        if (Files.exists(filePath)) {
            loadJsonMob(filePath);
            log.info("[MobManager] {} 로드 완료", filename);
        } else {
            log.warn("[MobManager] 파일을 찾을 수 없습니다: {}", filename);
        }
    }
}
