# 📅 DevLog: Economy Schema Improvement
#Economy #MongoDB #Schema #Refactoring

## 📝 주제: 경제 데이터 스키마 개선 (Traceability & Manageability)

> [!INFO] **요약**
> MongoDB에 저장되는 경제 데이터의 가독성과 추적 가능성을 높이기 위해 스키마를 개선했습니다.
> `uuid` 외에 `name`, `created_at`, `updated_at` 필드를 추가하여 데이터 관리 및 분석의 기반을 마련했습니다.

---

### 1. ⚠️ Problem & Motivation
**"DB를 열어봤는데 누가 부자인지 알 수가 없다..."**

- **가독성 문제**: 기존에는 `uuid`, `balance`, `cash`만 저장되었습니다. DB 관리 도구(Compass 등)에서 데이터를 볼 때, UUID만으로는 누구의 계정인지 식별하기가 매우 번거로웠습니다.
- **데이터 추적 불가**: 언제 가입했는지(`created_at`), 언제 마지막으로 거래했는지(`updated_at`)에 대한 정보가 없어, 장기 미접속 유저 파악이나 데이터 분석이 불가능했습니다.

### 2. 💡 Solution
필수 메타데이터 3가지를 추가하기로 결정했습니다.

1. **`name`**: 플레이어 닉네임. (검색 및 식별 용도)
2. **`created_at`**: 계정 생성 시각. (가입일 분석)
3. **`updated_at`**: 마지막 잔액 변동 시각. (휴면 계정 필터링)

### 3. 🛠️ Implementation Details

#### A. Interface Update (`EconomyStrategy`)
모든 경제 전략 구현체가 플레이어 이름을 알 수 있도록 인터페이스를 수정했습니다.

```java
// Before
void createAccount(UUID uuid, long initialAmount);

// After
void createAccount(UUID uuid, String name, long initialAmount);
```

#### B. Event Integration (`EconomyFeature`)
`AsyncPlayerConfigurationEvent`에서 플레이어의 최신 닉네임을 가져와 서비스에 전달합니다.

```java
server.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
    // 접속 시마다 닉네임 정보 전달
    economyService.createAccount(event.getPlayer().getUuid(), event.getPlayer().getUsername());
});
```

#### C. MongoDB Implementation (`MongoEconomyStrategy`)
- **생성 시**: `createAccount` 호출 시 `name`, `created_at`, `updated_at`을 함께 insert 합니다.
- **접속 시**: 이미 계정이 있다면 `name`을 최신값으로 업데이트합니다 (닉네임 변경 대응).
- **거래 시**: 입금/출금/설정(`deposit`, `withdraw`, `set`) 발생 시 `updated_at` 필드를 현재 시간으로 갱신합니다.

```java
// MongoDB Update Example
collection.updateOne(
    Filters.eq("uuid", uuid.toString()),
    Updates.combine(
        Updates.inc(field, amount),
        Updates.set("updated_at", new Date()) // 항상 최신화
    )
);
```

#### D. Event Listener Refactoring & Optimization (TTL)
리스너 코드가 복잡해지는 것을 막기 위해 `EconomyConnectionListener`로 분리했습니다.
또한, 메모리 효율성을 위해 **"접속 종료 후 1시간 뒤 캐시 만료"** 기능을 추가했습니다.

```java
public class EconomyConnectionListener {
    // ...
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        // 접속 종료 시 Redis 데이터가 3600초(1시간) 후 만료되도록 설정
        // 재접속하면 다시 로드되므로 데이터 유실 없음
        economyService.expireAccountCache(event.getPlayer().getUuid(), 3600);
    }
}
```

### 4. 🚀 Next Steps

- [ ] **데이터 마이그레이션**: 기존에 생성된 계정들은 `name`과 `created_at` 필드가 없을 수 있습니다. 접속 시 자동으로 채워지도록 로직을 구성했지만, 대규모 분석이 필요하다면 별도 마이그레이션 스크립트가 필요할 수 있습니다.
- [x] **Admin Command**: 닉네임 기반의 명령어(`/eco give <name> <amount>`) 지원을 위해, `uuid` 대신 `name`으로 조회하는 인덱스 추가를 고려해볼 만합니다.

### 5. 🔄 Refactoring: Event Registration Improvement

이벤트 리스너의 등록 방식을 개선하여 코드의 응집도를 높였습니다.

#### A. Problem
기존에는 `EconomyFeature`에서 리스너 객체를 생성하고, 수동으로 `addListener`를 호출하여 이벤트를 등록했습니다. 이로 인해 리스너 클래스는 단순히 메서드만 가지고 있고, 등록 로직이 외부(Feature)에 노출되는 구조였습니다.

#### B. Solution (`EconomyConnectionListener`)
생성자에서 `EventNode`를 주입받아 스스로 이벤트를 등록하도록 변경했습니다.

```java
public EconomyConnectionListener(EconomyService economyService, EventNode<Event> eventNode) {
    this.economyService = economyService;
    // 스스로 이벤트 등록
    eventNode.addListener(AsyncPlayerConfigurationEvent.class, this::onPlayerJoin);
    eventNode.addListener(PlayerDisconnectEvent.class, this::onPlayerQuit);
}
```

#### C. Integration (`EconomyFeature`)
Feature 클래스에서는 전역 이벤트 핸들러(`server.getGlobalEventHandler()`)를 넘겨주기만 하면 됩니다.

```java
// Before
EconomyConnectionListener listener = new EconomyConnectionListener(service);
server.getGlobalEventHandler().addListener(..., listener::onPlayerJoin);

// After
new EconomyConnectionListener(economyService, server.getGlobalEventHandler());
```
