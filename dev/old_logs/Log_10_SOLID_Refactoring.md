#Architecture #Refactoring #SOLID #Brainstorming

> [!INFO] **문서 개요**
> 
> - **목표:** SOLID 원칙 위반 코드 구조 개선 및 모듈 간 결합도 감소
>     
> - **핵심 변경:** 생명주기 버그 수정, 의존성 역전(DIP) 적용
>     
> - **연동 시스템:** [[Economy System]], [[Shop System]], [[Rank System]]
>     

---

## 1. 문제 발견 (Brain Dump)

### 1.1. 분석 결과 요약

코드베이스 전반에 대한 SOLID 분석 결과, **등급 C**로 평가됨.

- **심각한 버그:** `Main.java`에서 `RankFeature`가 **두 번** 초기화됨
- **컴파일 오류 위험:** `ShopFeature`가 존재하지 않는 `getEconomyStrategy()` 호출
- **높은 결합도:** 구체 클래스에 직접 의존, 저수준 모듈(`Strategy`)에 직접 접근

### 1.2. 마인드맵

```mermaid
mindmap
  root((SOLID 문제))
    DIP 위반
      ShopFeature
        EconomyFeature 구체 클래스 의존
        EconomyStrategy 직접 접근
      Main.java
        모든 Feature를 new 키워드로 생성
    SRP 위반
      Main.java
        서버 설정 + 인스턴스 생성 + 이벤트 등록 + 수동 연결
      ShopFeature.handlePurchase
        비즈니스 로직 + 프레젠테이션 로직 혼합
    Lifecycle Bug
      RankFeature
        enable() 두 번 호출
        이벤트 리스너 중복 등록
```

---

## 2. 리팩토링 설계 ([[Refactoring Plan]])

### 2.1. 핵심 원칙

> [!TIP] **의존성 역전 (DIP)**
> - 고수준 모듈(`ShopFeature`)은 저수준 모듈(`EconomyStrategy`)에 직접 의존하지 않는다.
> - 둘 다 추상화(`EconomyService`)에 의존한다.

### 2.2. 변경 Point

| 파일 | 문제점 | 해결책 |
|------|--------|--------|
| `Main.java:44` | `rankFeature.enable()` 중복 호출 | **삭제** (루프에서 자동 호출됨) |
| `EconomyFeature` | Service 접근자 없음 | `getEconomyService()` 메서드 **추가** |
| `ShopFeature` | `getEconomyStrategy()` 호출 (컴파일 오류) | `economyService.xxx()` 로 **변경** |

---

## 3. 적용된 변경 사항

### 3.1. `Main.java` - 생명주기 버그 수정

**Before (버그 코드):**
```java
RankFeature rankFeature = new RankFeature();
rankFeature.enable(minecraftServer, null); // ❌ 수동 호출

features.add(rankFeature);
for (GameFeature feature : features) {
    feature.enable(...); // ❌ 중복 호출 발생!
}
```

**After (수정 후):**
```java
RankFeature rankFeature = new RankFeature();
// enable() 호출 제거 ✅

features.add(rankFeature);
for (GameFeature feature : features) {
    feature.enable(...); // ✅ 여기서 한 번만 호출
}
```

---

### 3.2. `EconomyFeature` - Service 접근자 추가

[[Economy System]]의 `EconomyService`를 외부에서 사용할 수 있도록 Getter 추가.

```diff
public class EconomyFeature implements GameFeature {
    private EconomyService economyService;
    
    // ... enable/disable ...

+   /**
+    * EconomyService를 반환합니다.
+    */
+   public EconomyService getEconomyService() {
+       return economyService;
+   }
}
```

---

### 3.3. `ShopFeature` - DIP 적용

**Before (디미터 법칙 위반):**
```java
// Feature -> Feature -> Strategy (3단계 체인)
economyFeature.getEconomyStrategy().withdraw(...);
```

**After (정상적인 의존성):**
```java
// Feature -> Service (1단계)
private EconomyService economyService;

@Override
public void enable(...) {
    this.economyService = economyFeature.getEconomyService();
}

void handlePurchase(...) {
    economyService.withdraw(...); // ✅
}
```

---

## 4. 남은 과제 (Follow-up)

- [x] `ShopManager`에서도 `EconomyService` 직접 주입받도록 변경 ✅ Phase 2에서 완료
- [x] `Main.java`의 수동 연결(Wiring) 로직을 `FeatureManager`로 분리 ✅ Phase 2에서 완료
- [x] 상점 식별 방식을 Title 문자열 비교 → `Map<Inventory, Shop>` 캐시 방식으로 변경 ✅ Phase 2에서 완료

---

## 5. Phase 2: 고도화 리팩토링

### 5.1. `FeatureManager` 도입 (SRP 개선)

`Main.java`가 담당하던 Feature 생명주기 관리를 전담 클래스로 분리.

**새 파일:** `common/managers/FeatureManager.java`

```java
public class FeatureManager {
    private final List<GameFeature> features = new ArrayList<>();
    
    public void register(GameFeature feature) { ... }
    public void enableAll(Lamp<MinestomCommandActor> lamp) { ... }
    public void disableAll() { ... }
}
```

**Main.java 변경:**
```diff
- private static final List<GameFeature> features = new ArrayList<>();
+ FeatureManager featureManager = new FeatureManager(minecraftServer);

- features.add(rankFeature);
+ featureManager.register(rankFeature);

- for (GameFeature f : features) { f.enable(...); }
+ featureManager.enableAll(lamp);
```

---

### 5.2. `ShopManager` DIP 적용

`EconomyFeature`(구체 클래스) 대신 `EconomyService`(로직)를 주입받도록 변경.

```diff
- private final EconomyFeature economyFeature;
+ private final EconomyService economyService;

- public ShopManager(EconomyFeature economyFeature) { ... }
+ public ShopManager(EconomyService economyService) { ... }
```

---

### 5.3. Magic String 제거

Title 문자열 비교 방식 → `Map<Inventory, Shop>` 객체 참조 방식으로 변경.

**ShopManager에 추가:**
```java
private final Map<Inventory, Shop> openShopInventories = new ConcurrentHashMap<>();

public Shop getShopByInventory(Inventory inventory) {
    return openShopInventories.get(inventory);
}

public void untrackInventory(Inventory inventory) {
    openShopInventories.remove(inventory);
}
```

**ShopFeature 변경:**
```diff
- String title = PlainTextComponentSerializer.plainText().serialize(inventory.getTitle());
- Shop shop = shopManager.getShopByDisplayName(title);
+ Shop shop = shopManager.getShopByInventory(inventory);
```

---

### 🔗 연결된 문서

- [[Economy System]]: 경제 모듈 구조.
- [[Shop System]]: 상점 기능 설계.
- [[SOLID Analysis]]: 분석 보고서 원본.

