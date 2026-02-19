package org.mcuniverse.plugins.cosmetics.api;

public enum CosmeticType {
    TITLE("칭호"),
    DISPLAY_TAG("훈장"),
    COLOR_CHAT("채팅 색상"),
    SUPER_NICK("슈퍼 닉네임"),
    // Wardrobe Sub-types (동시 장착을 위해 분리)
    WARDROBE_HAT("모자"),
    WARDROBE_BACK("등 장식"),
    WARDROBE_LEFT_HAND("왼손 장식"),
    WARDROBE_BALLOON("풍선");

    private final String displayName;

    CosmeticType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}