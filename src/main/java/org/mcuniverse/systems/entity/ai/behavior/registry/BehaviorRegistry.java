package org.mcuniverse.systems.entity.ai.behavior.registry;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.mcuniverse.systems.entity.ai.behavior.BehaviorGroup;
import org.mcuniverse.systems.entity.ai.behavior.EntityBehavior;
import org.mcuniverse.systems.entity.ai.behavior.impl.IdleBehavior;
import org.mcuniverse.systems.entity.ai.behavior.impl.MeleeChaseAI;

public class BehaviorRegistry {

    private static final Map<String, Supplier<EntityBehavior>> registry = Map.of(
            "idle", IdleBehavior::new,
            "melee_chase", MeleeChaseAI::new);

    private BehaviorRegistry() {
    }

    public static BehaviorGroup resolve(String aiType) {
        Supplier<EntityBehavior> supplier = registry.getOrDefault(aiType, IdleBehavior::new);
        return new BehaviorGroup(List.of(supplier.get()));
    }

}
