package org.mcuniverse.systems.resourcepack;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class ResourcepackConfig {

    private static final Logger log = LoggerFactory.getLogger(ResourcepackConfig.class);

    private URI uri;
    private String hash;
    private boolean forced;
    private String prompt;

    public ResourcepackConfig() {
        Yaml yaml = new Yaml();
        try (InputStream input = new FileInputStream("systems/resourcepack/config.yml")) {
            Map<String, Object> data = yaml.load(input);
            Map<String, Object> rp = (Map<String, Object>) data.get("resourcepack");

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

    public String getHash() {
        return hash;
    }

    public boolean isForced() {
        return forced;
    }

    public String getPrompt() {
        return prompt;
    }
}
