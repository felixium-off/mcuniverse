# 📅 DevLog: Shop System Architecture Design
#Architecture #Shop #Economy #Cosmetics #DesignPattern

## 📝 주제: 상점 시스템 설계 (Shop System Architecture)

> [!INFO] **요약**
> **Economy(재화)**와 **Cosmetics(상품)**을 연결하는 **Shop System**의 아키텍처를 설계했습니다.
> 확장성을 고려하여 **Product 인터페이스**를 도입하고, 각 모듈 간의 결합도를 낮추는 방향으로 구조를 잡았습니다.

---

### 1. 🎯 Goal: 무엇을 만드는가?
플레이어가 자신이 가진 **돈(Economy)**을 사용하여 **치장 아이템(Cosmetics)**이나 기타 상품을 구매할 수 있는 시스템입니다.
핵심은 **"무엇이든 팔 수 있는 상점"**을 만드는 것입니다. (치장뿐만 아니라 등급, 버프 등)

### 2. 🧠 Brainstorming & Design Decisions

#### 2.1. 모듈 간의 관계 (Dependency)
상점은 두 가지 성격의 모듈을 연결하는 **중개자(Bridge)** 역할을 합니다.
- **Economy**: "얼마인가?" (지불 수단)
- **Cosmetics**: "무엇을 주는가?" (보상)

따라서 `Shop` 모듈은 `Economy`와 `Cosmetics` 모듈에 **의존(Dependency)**해야 합니다.
반대로, `Economy`나 `Cosmetics`는 `Shop`의 존재를 몰라도 독립적으로 동작해야 합니다. (단방향 의존성)

```mermaid
graph TD
    Shop[Shop System] -->|Uses| Economy[Economy System]
    Shop -->|Uses| Cosmetics[Cosmetics System]
    Shop -->|Uses| Rank[Rank System (Future)]
```

#### 2.2. 추상화 (Product Interface)
가장 큰 고민은 "나중에 랭크(Rank)도 팔고 싶다면?" 이었습니다.
`CosmeticShop`, `RankShop`을 따로 만드는 것은 비효율적입니다.
따라서 **전략 패턴(Strategy Pattern)**을 변형하여 `Product` 인터페이스를 설계했습니다.

```java
public interface Product {
    String getName();        // 상품명
    long getPrice();         // 가격
    ItemStack getIcon();     // GUI 표시용 아이콘
    
    // 핵심: 구매 시 일어나는 일은 구현체가 정의한다!
    void onPurchase(Player player); 
}
```

#### 2.3. 폴더 구조 (Package Structure)
`impl` 패키지를 두어 구체적인 상품 구현체들을 관리합니다.

```text
org.mcuniverse.shop
├── ShopFeature.java          # 모듈 진입점
├── ShopManager.java          # 상품 캐싱 및 관리
├── menu/                     # GUI 관련
│   ├── ShopMainMenu.java
│   └── ProductListMenu.java
└── model/
    ├── Product.java          # 인터페이스
    └── impl/
        ├── CosmeticProduct.java # 치장 아이템용 상품 (Cosmetics 연결)
        └── RankProduct.java     # 등급 상품 (Rank 연결 - 추후 추가)
```

### 3. 🚀 Implementation Steps
1.  **`ShopFeature`**: 기능 초기화 및 의존성 주입.
2.  **`Product` Interface**: 유연한 상품 정의.
3.  **`CosmeticProduct`**: 실제 `Cosmetics` 모듈의 API를 호출하여 아이템 지급.
4.  **`ShopGUI`**: Minestom Inventory를 활용한 시각적 상점 구현.

### 4. 💡 Expectation
이 구조가 완성되면, 기획자가 "새로운 치장 아이템 팔고 싶어요"라고 했을 때:
1.  `Cosmetics`에 아이템 추가.
2.  `Shop` 설정(Config or DB)에 해당 아이템 ID와 가격 등록.
끝입니다. 코드를 수정할 필요 없이 데이터 기반으로 상점이 돌아가게 됩니다.
