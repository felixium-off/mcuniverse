package org.mcuniverse.decorator.api;

public enum CosmeticType {
    TITLE("칭호"),
    TRAIL("파티클 효과"),
    GADGET("장난감"),
    WARDROBE("의상");

    private final String displayName;

    CosmeticType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}