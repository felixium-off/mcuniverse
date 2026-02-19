# 📅 DevLog: Database Optimization & Caching
#Database #Redis #MongoDB #Async #Performance

## 📝 주제: 고성능 DB 아키텍처 (Redis + MongoDB)

> [!SUCCESS] **성과 요약**
> **Redis(읽기/쓰기)**와 **MongoDB(영구저장)**를 하이브리드로 사용하여 **속도(0ms 응답)**와 **안정성(데이터 보존)**을 모두 잡았습니다.
> **Lua Script**로 동시성 문제를 해결하고, **Graceful Shutdown**으로 데이터 유실을 방지했습니다.

---

### 1. 🧠 Brainstorming: 느린 DB, 어떻게 해결할까?

> [!FAILURE] **The Problem**
> MongoDB Atlas가 서울 리전에 있어도 RTT가 **447ms**나 나옴.
> 유저가 돈을 입금했는데 0.5초 뒤에 반응하면 게임 망함.

- **아이디어**: "메모리(RAM)는 빠르잖아? Redis를 캐시로 쓰자!"
- **전략**:
    - **Read**: Redis 먼저 보고, 없으면 DB에서 가져온다. (Lazy Loading)
    - **Write**: Redis에 먼저 쓰고, DB에는 천천히(비동기) 저장한다. (Write-Behind)

### 2. Architecture & Patterns

#### 🚀 Read-Through & Write-Behind
1. **User Action**: `/eco deposit`
2. **Redis**: 즉시 값 변경 (0ms) -> 유저는 "오 빠르네" 느낌.
3. **Async Thread**: `CompletableFuture`가 백그라운드에서 MongoDB에 `Update` 쿼리 날림.

#### 🛡️ DatabaseManager (Singleton)
- **ExecutorService**: DB 작업만 전담하는 스레드 풀 관리. (직원 4명 고용)
- **Graceful Shutdown**: 서버 꺼질 때, "야! 하던 거 다 끝내고 퇴근해!"라고 명령. (비동기 작업 완료 대기)

### 3. Critical Solved Problems

> [!DANGER] **동시성 문제 (Concurrency)**
> 돈을 `get`하고 `set`하는 사이에 다른 요청이 들어오면 돈이 복사되거나 마이너스가 됨.
> **Solution**: `Redis Lua Script` 사용.
> - "조회+검증+수정"을 **단 하나의 원자적(Atomic) 연산**으로 처리. 절대 끼어들 수 없음.

```lua
-- Lua Script Logic
local current = redis.call('HGET', KEYS[1], ARGV[1])
if not current then current = 0 end
if tonumber(current) + tonumber(ARGV[2]) < 0 then return 0 end -- 잔액 검증
redis.call('HINCRBY', KEYS[1], ARGV[1], ARGV[2]) -- 수행
return 1
```

### 4. 🔗 System Relationships & Gap Analysis

> [!NOTE] **Backbone of All Systems**
> 이 DB 모듈은 모든 시스템의 기반이 됩니다.
> - **With Economy [[Log_01_Economy_System]]**: 돈 데이터 저장의 핵심.
> - **With Permissions [[Log_02_Permission_Essentials]]**: 향후 랭크/권한 데이터도 Redis에 캐싱해야 함. 현재는 `Memory`지만 마이그레이션 권장.
> - **With Cosmetics [[Log_03_Cosmetic_Architecture]]**: 치장 데이터(`equippedIds`)도 Redis Set 자료구조 활용 시 속도 향상 가능.

### 5. Future Improvements
- [ ] **Redis Pub/Sub**: 서버가 여러 대(Multi-Channel)일 때 데이터 동기화 구현.
- [ ] **Batch Processing**: DB 저장을 건건이 하지 말고, 1분마다 모아서 한 번에(Bulk Write) 하면 DB 부하 감소 가능.