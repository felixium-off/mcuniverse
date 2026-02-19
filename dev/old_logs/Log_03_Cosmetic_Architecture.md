# 📅 DevLog: Cosmetic System Architecture
#Cosmetics #OCP #EntityHandling #DecoratorPattern

## 📝 주제: 확장 가능한 치장 시스템 (OCP 준수)

> [!INFO] **요약**
> 유저의 개성을 표현할 **치장(Cosmetic)** 시스템을 설계했습니다.
> **Open-Closed Principle (OCP)**을 철저히 준수하여, 새로운 치장 종류(펫, 이펙트 등)가 추가되어도 기존 매니저 코드는 수정되지 않도록 구조를 잡았습니다.

---

### 1. 🧠 Brainstorming: 치장 시스템의 미래

**"치장 아이템이 100개가 넘어가면 관리를 어떻게 하지?"**

- 하나하나 `if-else`로 끼우면 망함.
- 부위(Type)별로 슬롯을 나누자. (모자, 등, 손, 칭호...)
- **추상화(Abstraction)**: `Cosmetic`이라는 인터페이스 하나로 모든 치장을 다루자.

### 2. Architecture Overview

> [!TIP] **Package Design**
> - `registry`: 치장 아이템 등록소 (쇼룸)
> - `manager`: 장착/해제 관리자 (탈의실)
> - `impl`: 실제 옷들 (구현체)

#### Class Hierarchy
- **Interface**: `Cosmetic` (전략)
    - `onEquip(player)`
    - `onUnequip(player)`
- **Enum**: `CosmeticType`
    - `TITLE` (칭호)
    - `DISPLAY_TAG` (머리 위 훈장)
    - `COLOR_CHAT` (채팅 색깔)
    - `WARDROBE_HAT` (블록벤치 모자)
    - `WARDROBE_BACK` (날개/가방)

### 3. Implementation Details

#### 🎨 Wardrobe (Blockbench)
- **ItemDisplay** 엔티티를 활용.
- 플레이어의 위치를 따라다니게 하거나(`Teleport`), `Passenger`로 탑승시켜 구현.
- **Issue**: 텔레포트 방식은 틱(Tick) 밀림이 있을 수 있음. 패킷(Packet) 단위로 따라다니게 하는 것이 가장 부드러움.

#### 💬 ColorChat
- `AsyncChatEvent`를 리스닝.
- 입장 시(`Join`) 플레이어에게 배정된 색상 코드를 캐싱해두고, 채팅 칠 때마다 적용.

### 4. 🔗 System Relationships & Gap Analysis

> [!WARNING] **Dependency & Optimization Check**
> 1. **Data Storage**: 치장 데이터는 영구 저장되어야 함. -> [[Log_04_Database_Optimization]]의 `MongoRepository` 사용 필수.
> 2. **Economy Link**: 치장은 대부분 유료임. 구매 시 `withdraw` 호출 필요. -> [[Log_01_Economy_System]]과 강한 결합 예상.
> 3. **Performance**: 파티클(Trail)이나 엔티티(Wardrobe)가 많아지면 클라이언트 렉 발생 가능.
>    - **LOD (Level of Detail)** 적용 고려: 멀리 있는 플레이어의 치장은 숨기기.

### 5. Next Actions
- [ ] **CosmeticRegistry**에 더미 데이터 넣고 테스트
- [ ] **MongoDB**에 치장 데이터 저장하는 로직 구현 (Async)
- [ ] 블록벤치 모델(`ItemDisplay`) 소환 테스트