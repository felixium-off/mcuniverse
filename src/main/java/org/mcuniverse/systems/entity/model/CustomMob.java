package org.mcuniverse.systems.entity.model;

import java.util.function.Consumer;

import org.mcuniverse.systems.entity.ai.behavior.registry.BehaviorRegistry;
import org.mcuniverse.systems.entity.data.MobDTO;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class CustomMob extends BaseMob {
    public CustomMob(MobDTO mob) {
        super(mob);

        applyEquip();
        applySimpleAI();
    }

    private void applyEquip() {
        var eq = getMob().getEquipment();
        if (eq == null)
            return;

        applySlot(eq.getMainHand(), this::setItemInMainHand);
        applySlot(eq.getHelmet(), this::setHelmet);
        applySlot(eq.getChestplate(), this::setChestplate);
        applySlot(eq.getLeggings(), this::setLeggings);
        applySlot(eq.getBoots(), this::setBoots);
    }

    private void applySlot(String name, Consumer<ItemStack> setter) {
        var material = Material.fromKey("minecraft:" + name.toLowerCase());
        if (material != null)
            setter.accept(ItemStack.of(material));
    }

    private void applySimpleAI() {
        if (getMob().getAiType() != null) {
            var aiType = getMob().getAiType();
            var aiOptions = getMob().getAiOptions();
            BehaviorRegistry.resolve(aiType, aiOptions).attach(this);
        }
    }
}
