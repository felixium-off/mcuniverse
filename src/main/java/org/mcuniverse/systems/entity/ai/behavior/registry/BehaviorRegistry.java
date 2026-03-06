package org.mcuniverse.systems.entity.ai.behavior.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.mcuniverse.systems.entity.ai.behavior.BehaviorGroup;
import org.mcuniverse.systems.entity.ai.behavior.EntityBehavior;
import org.mcuniverse.systems.entity.ai.behavior.impl.IdleBehavior;
import org.mcuniverse.systems.entity.ai.behavior.impl.MeleeChaseAI;
import org.mcuniverse.systems.entity.ai.behavior.impl.PassiveFollowAI;

public class BehaviorRegistry {

    private static final Map<String, Function<Map<String, Object>, EntityBehavior>> registry = new HashMap<>();

    static {
        registry.put("idle", opts -> new IdleBehavior());
        registry.put("melee_chase", MeleeChaseAI::new);
        registry.put("passive_follow", PassiveFollowAI::new);
    }

    private BehaviorRegistry() {
    }

    public static BehaviorGroup resolve(String aiType, Map<String, Object> options) {
        Function<Map<String, Object>, EntityBehavior> fn = registry.getOrDefault(aiType, opts -> new IdleBehavior());
        return new BehaviorGroup(List.of(fn.apply(options)));
    }

}
