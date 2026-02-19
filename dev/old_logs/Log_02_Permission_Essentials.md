# 📅 DevLog: Permissions & Essentials
#Permissions #LampFramework #Commands #Administrative

## 📝 주제: 권한 관리와 기초 명령어 (Lamp Framework)

> [!INFO] **요약**
> **Lamp** 프레임워크를 도입하여 어노테이션(`@Annotation`) 기반의 깔끔한 권한 관리를 구현했습니다.
> 또한, 서버 운영에 필수적인 **Essentials** 기능(워프, 홈, 키트)을 모듈화하여 개발했습니다.

---

### 1. 🧠 Brainstorming: 권한을 어떻게 제어할까?

- **기존 방식**: `if (player.hasPermission("admin.eco"))` 도배... 🤮
- **개선 목표**: 메소드 위에 딱지(Annotation)만 붙이면 알아서 검사해주면 좋겠다.
- **해결책**:
    - **Lamp Framework** 사용.
    - `@RequiresRank(Rank.ADMIN)` 커스텀 어노테이션 구현.

### 2. Implementation Details

#### 🔑 Custom Annotation Flow
1. **User Command** -> `/eco` 입력
2. **Lamp Interceptor** -> 가로채기
3. **Check Rank** -> 플레이어의 메모리 상 Rank 확인 (e.g., `Rank.USER` vs `Rank.ADMIN`)
4. **Result** -> 통과 시 메소드 실행, 실패 시 "권한이 없습니다." 메시지 자동 반환.

```java
@IsOpped
@Command("eco")
@RequiresRank(Rank.ADMIN) // <- 이 한 줄로 권한 체크 끝!
public void onDeposit(...) { ... }
```

### 3. Essentials Features Structure

기능의 복잡도에 따라 두 그룹으로 분리하여 **Single Responsibility Principle (SRP)** 준수.

| 분류 | 특징 | 예시 |
| :--- | :--- | :--- |
| **Simple Commands** | 상태 변경, 로직 단순, 저장 불필요 | `Gamemode`, `Speed`, `Heal`, `Fly` |
| **Data Features** | 파일/DB 저장 필수, 데이터 모델 필요 | `Warp` (좌표), `Home` (개인좌표), `Kit` (아이템) |

### 4. 🔗 System Relationships & Gap Analysis

> [!NOTE] **Integration Review**
> - **With Economy**: `Kit` 기능 구현 시 유료 키트(돈 내고 구매) 기능이 추가될 수 있음. -> [[Log_01_Economy_System]]과 연동 필요.
> - **With Database**: `Warp`, `Home` 데이터는 현재 파일(JSON)로 저장되나, 서버 규모가 커지면 DB로 이관해야 함. -> [[Log_04_Database_Optimization]]의 `DatabaseManager` 활용 가능.

### 5. Future Ideas (To-Do)
- [ ] **Rank 승급 시스템**: 특정 조건(플레이 타임, 돈) 달성 시 자동 랭크업
- [ ] **GUI 메뉴**: 명령어가 어려운 뉴비들을 위한 `/menu` 시스템 (Inventory GUI)