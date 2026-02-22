package org.mcuniverse.systems.resourcepack;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

@Slf4j
public class ResourcepackConfig {

    private URI uri;
    @Getter
    private String hash;
    @Getter
    private boolean forced;
    @Getter
    private String prompt;

    public ResourcepackConfig() {
        Yaml yaml = new Yaml();
        try (InputStream input = new FileInputStream("systems/resourcepack/config.yml")) {
            Map<String, Object> data = yaml.load(input);
            @SuppressWarnings("unchecked") Map<String, Object> rp = (Map<String, Object>) data.get("resourcepack");

            this.uri = URI.create((String) rp.get("url"));
            this.hash = (String) rp.get("hash");
            this.forced = (boolean) rp.get("forced");
            this.prompt = (String) rp.get("prompt");

            log.info("\n[RESOURCEPACK] config loaded. -> {}", this.uri);
        } catch (Exception e) {
            log.error("[RESOURCEPACK] config load failed.", e);
        }
    }

    public @NotNull URI getUrl() {
        return uri;
    }
}
