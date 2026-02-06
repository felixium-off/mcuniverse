# 📅 DevLog: Unified Listener Architecture
#Architecture #Refactoring #DRY #EventHandling

## 📝 주제: 접속 리스너 통합 (Unified Connection Listener)

> [!INFO] **요약**
> `Economy`와 `Rank` 모듈에서 중복되어 사용되던 "접속/종료 이벤트 리스너"를 하나로 통합했습니다.
> 이를 위해 **`PlayerDataHandler` 인터페이스**와 **`CommonConnectionListener`**를 도입하여, 관심사를 분리하고 확장성을 확보했습니다.

---

### 1. ⚠️ Problem: 코드 중복과 유지보수 문제
기존에는 각 기능(`Rank`, `Economy`)마다 별도의 리스너(`RankConnectionListener`, `EconomyConnectionListener`)를 가지고 있었습니다.
하지만 이들의 역할은 100% 동일했습니다.
1. **접속(Join)**: 플레이어 데이터 로드 (DB -> Redis)
2. **종료(Quit)**: Redis 캐시 만료 시간 설정 (TTL)

새로운 시스템(예: Level, Guild)이 추가될 때마다 똑같은 리스너 코드를 복사해서 만들어야 하는 **비효율성(Violating DRY)**이 발생했습니다.

### 2. 💡 Solution: 전략 패턴 같은 핸들러 도입
이 문제를 해결하기 위해 **"이벤트 감지"**와 **"데이터 처리"**를 분리했습니다.

#### 2.1. `PlayerDataHandler` Interface
데이터를 로드하고 언로드하는 행위 자체를 추상화했습니다.
```java
public interface PlayerDataHandler {
    void onLoad(Player player);   // 접속 시 실행할 로직
    void onUnload(Player player); // 종료 시 실행할 로직
}
```

#### 2.2. `CommonConnectionListener`
Minestom의 이벤트를 실제로 듣는 구현체는 **단 하나**만 존재합니다.
이 리스너는 등록된 여러 `PlayerDataHandler`들을 순회하며 이벤트를 전파합니다.

```java
// CommonConnectionListener.java
public void onJoin(Event event) {
    for (PlayerDataHandler handler : handlers) {
        handler.onLoad(event.getPlayer());
    }
}
```

### 3. ✨ Implementation Details
각 Feature(`RankFeature`, `EconomyFeature`)는 이제 별도의 리스너 클래스를 만들지 않고, **익명 클래스(Anonymous Class)**나 **람다** 형태(추후 적용 가능)로 핸들러만 등록하면 됩니다.

```java
// EconomyFeature.java Example
CommonConnectionListener listener = new CommonConnectionListener(eventNode);
listener.addHandler(new PlayerDataHandler() {
    @Override
    public void onLoad(Player player) {
        economyService.createAccount(player.getUuid(), player.getName());
    }
    @Override
    public void onUnload(Player player) {
        economyService.expireAccountCache(player.getUuid(), 3600);
    }
});
```
*(참고: 현재는 Feature 독립성을 위해 각자 Listener 인스턴스를 생성하지만, 로직은 성공적으로 분리되었습니다. 추후 Main에서 중앙 관리하도록 개선할 수 있습니다.)*

### 4. 🔗 Connected Changes
- **Rank Module Upgrade**: 통합을 위해 `Rank` 시스템에도 `name` 저장 기능과 `TTL`(만료) 기능이 추가되었습니다.
- **Main.java Cleanup**: `Main.java`에 하드코딩 되어있던 `rankService.createRank()` 호출을 제거하고, 리스너 내부로 이동시켜 응집도를 높였습니다.
