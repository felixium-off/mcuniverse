package org.mcuniverse.systems.resourcepack;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;

public class ResourcepackService {

    private final ResourcePackRequest request;

    public ResourcepackService(ResourcepackConfig config) {
        UUID uuid = UUID.nameUUIDFromBytes(config.getHash().getBytes(StandardCharsets.UTF_8));

        ResourcePackInfo info = ResourcePackInfo.resourcePackInfo().id(uuid).uri(config.getUrl()).hash(config.getHash())
                .build();

        this.request = ResourcePackRequest.resourcePackRequest().packs(info).prompt(Component.text(config.getPrompt()))
                .required(config.isForced()).build();
    }

    public ResourcePackRequest getRequest() {
        return request;
    }
}
