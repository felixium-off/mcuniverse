# 📅 DevLog: Cache Lifecycle & The "Time Bomb" Bug
#Redis #Caching #BugFix #Performance

## 📝 주제: 캐시 수명 주기 관리와 시한폭탄 버그 해결

> [!DANGER] **Critical Bug Identified**
> 유저가 재접속했음에도 불구하고, 이전 세션에서 설정된 TTL(만료 시간)이 계속 흘러가다가 게임 도중 데이터가 증발하는 현상을 발견했습니다.
> 이를 해결하기 위해 **"Touch-on-Join (접속 시 생존 신고)"** 패턴을 도입했습니다.

---

### 1. 🕵️ The Scenario (Why is this a problem?)

Redis의 `Hash` 자료구조는 필드(`HSET`)를 수정해도 키 자체의 TTL(`EXPIRE`)이 초기화되지 않습니다. 이로 인해 다음과 같은 시나리오가 발생합니다.

1. **12:00** - 유저 A 로그아웃. (`expireAccountCache` 실행 -> TTL 1시간 설정)
2. **12:50** - 유저 A 재접속. (남은 TTL: 10분)
    - 기존 로직은 데이터가 존재하므로 DB 로드를 건너뜀.
    - **문제점**: TTL을 제거(`PERSIST`)하지 않음.
3. **12:55** - 유저가 상점을 이용함. (남은 TTL: 5분)
4. **13:00** - **💥 데이터 증발 (Eviction)**
    - 유저는 게임 중인데 Redis 키가 만료되어 사라짐.
5. **13:01** - 유저가 상점 이용 시도.
    - **Cache Miss** 발생 -> MongoDB에서 동기/비동기 로드 -> **순간적인 렉(Lag Spike)** 발생.

### 2. 💡 Solution: Keep-Alive Pattern

유저가 서버에 존재하는 동안에는 데이터가 절대 만료되지 않도록 보장해야 합니다.

#### A. Lifecycle Flow

| 상태 | 동작 | Redis 상태 |
| :--- | :--- | :--- |
| **Join** | `createAccount` -> **`redis.persist(key)`** | **Permanent (영구)** |
| **Play** | `get`, `set` -> `redis.persist(key)` (방어 코드) | **Permanent (영구)** |
| **Quit** | `onPlayerQuit` -> `redis.expire(key, 3600)` | **Volatile (1시간 뒤 만료)** |

#### B. Code Implementation (`MongoEconomyStrategy`)

```java
@Override
public void createAccount(UUID uuid, String name, long initialAmount) {
    String key = getKey(uuid);

    if (redis.exists(key) == 0) {
        // 신규 생성 or Cache Miss (DB 로드)
    } else {
        // [Fix] 유저가 돌아왔다! 시한폭탄 해제.
        redis.persist(key); 
        
        // 닉네임 동기화 등 후속 작업...
    }
}
```

### 3. 🧪 Verification

이 패치 이후의 예상 동작은 다음과 같습니다.

1. **12:00** - 로그아웃 (TTL 1시간 시작)
2. **12:50** - 재접속
    - `createAccount` 감지 -> `redis.persist` 실행.
    - TTL 제거됨 (TTL: -1).
3. **13:00** - 1시간이 지났지만 데이터는 **살아있음**.
4. **14:00** - 유저가 다시 로그아웃.
    - 다시 TTL 1시간 설정.

### 4. 🚀 Lessons Learned

> [!TIP] **Redis Expiration Rule**
> - `String` 타입은 `SET` 명령어로 값을 덮어쓰면 TTL이 사라집니다.
> - 하지만 `Hash`, `List`, `Set` 등은 내부 아이템을 수정해도 **키의 TTL은 유지**됩니다.
> - 따라서 명시적으로 `PERSIST`를 호출하여 "이 데이터는 이제 사용 중임"을 알려야 합니다.

---

**Related Logs:**
- [[Log_04_Database_Optimization]]: 초기 캐싱 전략 수립
- [[Log_05_Economy_Schema_Improvement]]: 리스너 구조 개선