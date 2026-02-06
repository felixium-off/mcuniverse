# 📅 DevLog: Shop GUI & Configuration Implementation
#Shop #GUI #YAML #Minestom

## 📝 주제: 상점 GUI 및 설정 구현

> [!INFO] **요약**
> 상점 시스템의 **GUI(Chest GUI)**와 **YAML 설정 로더**를 구현했습니다.
> 이제 `shop/products.yml` 파일을 통해 상품을 추가하고, `/shop` 명령어로 GUI를 열어 구매할 수 있습니다.

---

### 1. 🖼️ GUI Implementation (ShopMenu)
Minestom의 `Inventory` API를 사용하여 6줄짜리 가상 상자(Chest 6 Row) GUI를 만들었습니다.
- **아이콘**: `products.yml`에서 설정한 Material로 아이템 표시.
- **상호작용**: 클릭 시 `InventoryCondition`을 통해 이벤트를 감지하고 `Product.onPurchase()`를 호출합니다.
- **안전장치**: `setCancellation(true)`를 통해 아이템을 꺼내지 못하도록 막았습니다.

```java
// ShopMenu.java (Simplified)
inventory.addInventoryCondition((player, slot, clickType, result) -> {
    result.setCancel(true);
    if (isValidSlot(slot)) {
        products.get(slot).onPurchase(player);
    }
});
```

### 2. ⚙️ Configuration (ShopConfig)
상품 정보를 코드에 하드코딩하지 않고, `YAML` 파일로 관리할 수 있게 만들었습니다.
이를 위해 `SnakeYAML` 라이브러리를 의존성에 추가했습니다.

**[shop/products.yml Example]**
```yaml
products:
  - type: COSMETIC
    cosmetic_id: chat_red
    price: 1000
    icon: paper
```

### 3. 🧩 Integration (DecoratorFeature Link)
`ShopFeature`가 로드될 때, 상품 목록을 구성하기 위해 `DecoratorFeature`(치장 모듈)의 레지스트리가 필요했습니다.
이를 위해:
1.  `DecoratorFeature`에 싱글톤 패턴(`getInstance`)을 적용하여 어디서든 접근 가능하게 했습니다.
2.  `ShopConfig`에서 `CosmeticRegistry`를 통해 ID(`chat_red`)를 실제 `Cosmetic` 객체로 변환합니다.

### 4. 🚀 Next Steps
- **Economy Integration**: 현재는 구매 시 돈이 차감되지 않습니다. `ShopService`를 구현하여 실제 거래 로직을 추가해야 합니다.
- **Ownership Check**: 이미 구매한 상품은 "보유중"으로 표시하거나 구매를 막는 기능이 필요합니다.
