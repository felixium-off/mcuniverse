package org.mcuniverse.systems.entity.data;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PlayerDTO {

    // 기본정보
    private String id;
    private String displayName;

    private PlayerEquipment equipment;
    private PlayerStats stats;

    @Getter
    public static class PlayerEquipment {
        private String helmet;
        private String chestplate;
        private String leggings;
        private String boots;
        private String mainHand;
        private String offHand;
    }

    @Getter
    public static class PlayerStats {
        private int hp;
        private int attackDamage;
        private float critRate;
        private float critMultiplier;
        private float defense;
        private double movementSpeed;
    }
}
