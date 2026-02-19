#Shop #Minestom #Economy #Architecture #Brainstorming

> [!INFO] **문서 개요**
> 
> - **목표:** 인게임 GUI 기반의 직관적인 상점 편집 및 이용 시스템 구축
>     
> - **핵심 기술:** Minestom Inventory, SNBT Serialization, SnakeYAML
>     
> - **연동 시스템:** [[Economy System]], [[Rank System]]
>     

---

## 1. Core Concept (핵심 철학)

### 1.1. "Everything is GUI"

- **기존 문제:** YAML 파일을 직접 수정하다가 오타 발생, 서버 리로드 필요.
    
- **해결책:** `/shop edit` 명령어로 인벤토리를 열고, 아이템을 넣고 닫으면 **자동으로 SNBT 변환되어 저장**.
    
- **Admin UX:** "Drag & Drop to Save"
    

### 1.2. "No Data Loss" (데이터 무결성)

- **SNBT 사용:** 마인크래프트의 모든 NBT(인챈트, 커스텀 모델 데이터, 가죽 색상 등)를 문자열로 완벽하게 보존.
    
- **구조 분리:** `Config`(설정) / `View`(디자인) / `Products`(상품) 3단 분리로 유지보수성 극대화.
    

---

## 2. File Structure (데이터 설계)

[[Log_09_Shop_GUI_Implementation]]에서 정의한 구조를 확장하여 ExcellentShop 스타일로 고도화.

### 📂 Directory: `/shop/shops/<shop_id>/`

상점 하나당 하나의 '폴더'를 생성하여 관리.

#### 📄 `config.yml` (Meta Data)

상점의 **신분증**. 상품 정보는 없음.

YAML

```
displayName: "<Gradient>무기 상점</Gradient>"
permission: "shop.weapon.use"
npcId: 102 # Citizens 연동용 (선택)
tradeType: "BUY_ONLY" # BUY_ONLY, SELL_ONLY, BOTH
```

#### 📄 `view.yml` (Layout Design)

상점의 **인테리어**. 상품이 어디에 배치될지 결정하지 않음(동적 배치).

YAML

```
size: 54 # 6줄
title: "🗡️ 무기 상점"
patterns: # 배경 무늬 (유리판 등)
  border:
    item: "GRAY_STAINED_GLASS_PANE"
    slots: [0, 1, 2, ..., 53]
navigation:
  nextPage:
    slot: 50
    item: "ARROW"
  prevPage:
    slot: 48
    item: "ARROW"
```

#### 📄 `products.yml` (Database)

실제 **상품 목록**. [[Log_09]]의 내용을 반영하여 SNBT 적용.

YAML

```
products:
  'diamond_sword_epic': # 고유 ID
    slot: 10 # 고정 슬롯 (Optional)
    price: 5000
    currency: "balance" # [[Currency]] 인터페이스 연동
    stock: -1 # 무제한
    item_snbt: '{id:"minecraft:diamond_sword",Count:1b,tag:{display:{Name:\'{"text":"전설의 검"}\'}}}'
```

---

## 3. Class Diagram (객체 설계)

Minestom 환경에 맞춘 POJO 설계.

코드 스니펫

```
classDiagram
    class ShopManager {
        +Map<String, Shop> loadedShops
        +createShop(name, type)
        +loadShop(id)
        +openShop(player, id)
    }
    class Shop {
        -String id
        -ShopConfig config
        -ShopView view
        -Map<String, Product> products
    }
    class Product {
        -String id
        -ItemStack itemStack
        -Currency currency
        -double price
        -int stock
    }
    class ShopEditor {
        +openEditor(player, shop)
        +saveFromInventory(inventory)
    }

    ShopManager --> Shop : Manages
    Shop --> Product : Contains
    ShopEditor ..> Shop : Modifies
```

---

## 4. Command Flow (User Scenario)

### 4.1. 관리자: 상점 생성 및 편집 ([[Shop Admin]])

1. **생성:** `/shop create weapon balance`
    
    - `/shop/shops/balance_weapon/` 폴더 생성.
        
    - 기본 `config.yml`, `view.yml`, `products.yml` 생성.
        
2. **편집 진입:** `/shop edit weapon`
    
    - 빈 6줄 인벤토리 오픈.
        
3. **아이템 배치:**
    
    - 인벤토리에서 내가 만든 '전설의 검'을 슬롯에 넣음.
        
    - (추가 기획) 가격 설정은 어떻게?
        
        - **방법 A:** 아이템 넣을 때 `AnvilGUI`로 가격 입력.
            
        - **방법 B:** `/shop setprice <slot> <price>` 별도 명령어.
            
        - **방법 C (추천):** 아이템 Lore의 마지막 줄에 `Price: 1000` 적어놓고 넣으면 자동 파싱.
            
4. **저장:** 인벤토리 닫기 (ESC).
    
    - `InventoryCloseEvent` 감지 -> `ShopEditor.saveFromInventory()` 실행.
        
    - `ItemStack` -> SNBT 변환 -> `products.yml` 비동기 저장.
        

### 4.2. 유저: 상점 이용 ([[Shop User]])

1. **오픈:** `/shop open weapon` (또는 NPC 클릭)
    
2. **렌더링:**
    
    - `view.yml`의 배경 패턴 먼저 깔기.
        
    - `products.yml`의 아이템들을 `ItemStack.fromSNBT()`로 복구하여 배치.
        
    - 각 아이템 Lore에 가격 정보 추가 ("가격: 5000 골드").
        
3. **구매 시도:** 아이템 클릭.
    
    - **검증 1:** 인벤토리 공간 확인.
        
    - **검증 2:** [[Economy System]] 잔액 확인 (`hasAccount`, `getAccount`).
        
4. **트랜잭션 (Transaction):**
    
    - 돈 차감 (`withdraw`).
        
    - 아이템 지급 (`player.getInventory().addItemStack()`).
        
    - 로그 기록 (DB/File).
        

---

## 5. Implementation Roadmap (개발 순서)

- [ ] **Phase 1: 데이터 입출력 (I/O)**
    
    - [ ] `SnakeYAML`을 이용한 3단 파일 구조 로더 구현.
        
    - [ ] Minestom `TagStringIO`를 이용한 ItemStack ↔ SNBT 컨버터 구현.
        
- [ ] **Phase 2: 편집기 (Editor)**
    
    - [ ] `/shop edit` 명령어 및 GUI 구현.
        
    - [ ] 인벤토리 닫을 때 저장하는 로직 구현.
        
- [ ] **Phase 3: 이용자 로직 (User)**
    
    - [ ] `/shop open` 명령어 구현.
        
    - [ ] 아이템 클릭 리스너 및 구매/판매 로직 연결.
        
    - [ ] [[Currency]] 인터페이스와 연동하여 실제 돈 차감.
        
- [ ] **Phase 4: 고도화**
    
    - [ ] 페이징 시스템 (45개 이상 아이템 지원).
        
    - [ ] 재고(Stock) 시스템 연동 (Global/Player).
        

---

## 6. Technical Notes (기술적 고려사항)

> [!WARNING] **동시성 문제 (Concurrency)** 상점 파일 저장은 I/O 작업이므로 **반드시 비동기(Async)**로 처리해야 함. 하지만 Minestom의 인벤토리 접근은 메인 스레드여야 하므로, **"데이터 스냅샷 -> 비동기 저장"** 패턴을 사용할 것.

> [!TIP] **가격 설정 UX 아이디어** 편집 모드에서 아이템을 **Shift + 우클릭** 하면 채팅창에 "가격을 입력하세요"라고 띄우고, 채팅으로 숫자를 치면 가격이 설정되는 방식이 직관적임. (`AsyncPlayerChatEvent` 대신 마인스톰의 채팅 리스너 활용)

---

### 🔗 연결된 문서

- [[Economy System]]: 화폐 단위 및 잔액 처리.
    
- [[Database Manager]]: 데이터 비동기 처리 실행기.
    
- [[Rank System]]: 특정 랭크만 이용 가능한 상점 (VIP 전용).