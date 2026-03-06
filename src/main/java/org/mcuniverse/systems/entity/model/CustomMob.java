package org.mcuniverse.systems.entity.model;

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
        MobDTO.MobEquipment eq = getMob().getEquipment();
        if (eq == null)
            return;
        if (eq.getMainHand() != null) {
            Material m = Material.fromKey("minecraft:" + eq.getMainHand().toLowerCase());
            if (m != null)
                setItemInMainHand(ItemStack.of(m));
        }
        if (eq.getHelmet() != null) {
            Material m = Material.fromKey("minecraft:" + eq.getHelmet().toLowerCase());
            if (m != null)
                setHelmet(ItemStack.of(m));
        }
        if (eq.getChestplate() != null) {
            Material m = Material.fromKey("minecraft:" + eq.getChestplate().toLowerCase());
            if (m != null)
                setChestplate(ItemStack.of(m));
        }
        if (eq.getLeggings() != null) {
            Material m = Material.fromKey("minecraft:" + eq.getLeggings().toLowerCase());
            if (m != null)
                setLeggings(ItemStack.of(m));
        }
        if (eq.getBoots() != null) {
            Material m = Material.fromKey("minecraft:" + eq.getBoots().toLowerCase());
            if (m != null)
                setBoots(ItemStack.of(m));
        }
    }

    private void applySimpleAI() {
        if (getMob().getAiType() != null) {
            var aiType = getMob().getAiType();
            var aiOptions = getMob().getAiOptions();
            BehaviorRegistry.resolve(aiType, aiOptions).attach(this);
        }
    }
}
