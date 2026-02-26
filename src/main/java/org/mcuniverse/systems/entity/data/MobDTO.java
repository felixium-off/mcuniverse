package org.mcuniverse.systems.entity.data;

import java.util.List;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MobDTO {

    // 기본 정보
    private String id;
    private String displayName;
    private String bbModel;
    private String monsterType;

    // 그룹 및 태그
    private String group;
    private List<String> tags;
    private String aiType;

    // 중첩 객체
    private MobEquipment equipment;
    private MobStats stats;
    private MobSounds sounds;
    private MobRewards rewards;
    private String deathEffect;

    private String scriptHook;

    @Getter
    public static class MobEquipment {
        private String helmet;
        private String chestplate;
        private String leggings;
        private String boots;
        private String mainHand;
        private String offHand;
    }

    @Getter
    public static class MobStats {
        private int hp;
        private int attackDamage;
        private float critRate;
        private float critMultiplier;
        private float defense;
        private double movementSpeed;
    }

    @Getter
    public static class MobSounds {
        private MobSoundInfo death;
        private MobSoundInfo hurt;
        private MobSoundInfo ambient;

        @Getter
        public static class MobSoundInfo {
            private String sound;
            private float volume;
            private float pitch;
        }
    }

    @Getter
    public static class MobRewards {
        private MobExp exp;
        private MobMoney money;
        private List<MobDrops> drops;

        @Getter
        public static class MobExp {
            private int min;
            private int max;
        }

        @Getter
        public static class MobMoney {
            private int min;
            private int max;
        }

        @Getter
        public static class MobDrops {
            private String item;
            private int amount;
            private double chance;
        }
    }
}
