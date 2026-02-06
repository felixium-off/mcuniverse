# 📅 DevLog: Rank System Database Integration
#Rank #Redis #MongoDB #Refactoring

## 📝 주제: 랭크 시스템 DB 연동 (Economy 구조 계승)

> [!INFO] **요약**
> [[Log_04_Database_Optimization]]에서 구축한 Redis + MongoDB 하이브리드 아키텍처를 랭크 시스템에도 적용했습니다.
> 이제 플레이어의 등급(Rank) 정보가 영구 저장되며, 서버 재시작 후에도 유지됩니다.

---

### 1. 🎯 Objective
기존 랭크 시스템은 메모리나 파일 기반(Config)으로 동작할 것으로 가정되어 있었습니다.
하지만 멀티 서버 환경과 데이터 안정성을 위해 **DB 기반**으로 전환해야 합니다.
이미 검증된 **Economy System**의 저장소 패턴을 참고하여 빠르게 구현하는 것이 목표입니다.

### 2. ⚖️ Comparison: Economy vs Rank

두 시스템은 비슷해 보이지만 데이터의 성격이 다릅니다.

| 특징 | Economy (경제) | Rank (랭크) |
| :--- | :--- | :--- |
| **Data Type** | 숫자 (Balance) | 상태 (Enum: `USER`, `ADMIN`) |
| **Operations** | `Inc`, `Dec` (빈번함) | `Set` (가끔 발생) |
| **Concurrency** | **Critical** (돈 복사 방지) | Less Critical (덮어쓰기) |
| **Redis Key** | `economy:account:{uuid}` | `rank:account:{uuid}` |

### 3. 🛠️ Implementation Details (`MongoRankStrategy`)

#### A. Data Structure (Redis Hash)
확장성을 위해 Redis의 `String` 대신 `Hash` 구조를 선택했습니다.
추후 기간제 랭크(VIP) 등이 추가될 때 필드를 늘리기 쉽습니다.

```text
Key: rank:account:{uuid}
Field: "rank" -> Value: "ADMIN"
Field: "expiration" -> Value: "2026-12-31" (Future)
```

#### B. Logic Flow
1. **조회 (`getRank`)**: Redis 캐시 우선 조회 -> 없으면 DB 조회 및 캐싱 -> 없으면 기본값(`NEWBIE`) 반환.
2. **수정 (`setRank`)**: Redis 즉시 수정 -> DB 비동기(`CompletableFuture`) 업데이트.
3. **생성 (`createRank`)**: 접속 시 데이터가 없으면 초기값(`NEWBIE`)으로 DB/Redis 생성.

```java
// DB Update Logic
collection.updateOne(
    Filters.eq("uuid", uuid.toString()),
    Updates.combine(
        Updates.set("rank", rank.name()),
        Updates.set("updated_at", new Date())
    )
);
```

### 4. 🧠 Brainstorming & Next Steps

랭크 시스템이 DB와 연결되었으므로, 이제 더 고도화된 기능들을 기획할 수 있습니다.

#### A. 기간제 랭크 (Timed Ranks)
- **아이디어**: 상점에서 "VIP 30일권"을 구매.
- **구현**: Redis의 `Expire` 기능을 쓰거나, DB에 `expire_at` 필드를 추가하여 접속 시 체크.
- **Task**: `RankStrategy`에 `setRank(uuid, rank, duration)` 메서드 추가 검토.

#### B. 랭크 승급 시스템 (Auto Promotion)
- **아이디어**: 특정 조건 달성 시 자동 승급.
- **조건**:
    - 플레이 타임 100시간 이상
    - 소지금 100만 골드 이상 ([[Log_01_Economy_System]] 연동)
- **Task**: `RankPromotionFeature` 구현 필요.

#### C. 권한 동기화 (Permission Sync)
- 현재 [[Log_02_Permission_Essentials]]의 `Lamp` 프레임워크는 메모리 상의 랭크를 체크합니다.
- DB에서 불러온 랭크가 `Lamp`의 권한 핸들러(`RequiresRank`)와 즉시 연동되도록 리스너 점검이 필요합니다.

### 5. 🔗 Related Logs
- [[Log_01_Economy_System]]: 저장소 패턴의 원형.
- [[Log_04_Database_Optimization]]: DB 인프라 구축.

---

> [!TODO] **Immediate Action Items**
> - [ ] `RankConnectionListener` 구현: 접속 시 `createRank` 호출 (Economy와 동일 패턴).
> - [ ] `RankAdminCommand`: `/rank set <player> <rank>` 명령어 구현.
> - [ ] MongoDB Compass에서 데이터 저장 확인.