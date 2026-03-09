package org.mcuniverse.systems.entity.tag;

import net.minestom.server.tag.Tag;

public class AnimalTags {
    public static final Tag<Integer> STACK_SIZE = Tag.Integer("stack_size").defaultValue(1);
    public static final Tag<Integer> FED_COUNT = Tag.Integer("fed_count").defaultValue(0);
    public static final Tag<Long> LAST_PETTED_TIME = Tag.Long("last_petted_time").defaultValue(0L);
    public static final Tag<Integer> AFFECTION = Tag.Integer("affection").defaultValue(0);
    public static final Tag<String> OWNER_UUID = Tag.String("owner_uuid").defaultValue("");

    private AnimalTags() {
    }
}
