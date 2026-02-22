# MCUniverse

> Minestom 기반 게임 서버 — 1인 개발 중규모 프로젝트

---

## 기술 스택

| 역할 | 라이브러리 |
|---|---|
| 서버 코어 | [Minestom](https://github.com/Minestom/Minestom) `2025.12.20-1.21.4` |
| 명령어 | [Lamp](https://github.com/Revxrsal/Lamp) `4.0.0-rc.14` |
| 월드 포맷 | [Polar](https://github.com/hollow-cube/polar) `1.15.0` |
| NPC / 엔티티 | [WorldSeedEntityEngine](https://github.com/WorldSeedGames/WorldSeedEntityEngine) |
| 데이터베이스 | MongoDB (driver-sync `5.6.2`) |
| 캐시 | Redis (Lettuce `6.7.1`) |
| 설정 파일 | SnakeYAML `2.2` |
| 메트릭 | Micrometer + Prometheus |
| 코드 축약 | Lombok |

---

## 폴더 구조

```
src/main/java/org/mcuniverse/
│
├── Main.java                        # JVM 진입점 — new Server().start() 호출만
├── Server.java                      # 서버 생명주기 조율 (init → DB → register → start)
│
├── core/                            # 인프라 — 변경 빈도 가장 낮음
│   ├── config/                      # 설정 로더 (SnakeYAML 기반 .yml 파싱)
│   ├── database/
│   │   ├── mongo/                   # MongoConnect (싱글턴, 연결 풀)
│   │   └── redis/                   # RedisConnect (싱글턴, Lettuce 비동기)
│   └── exception/                   # 공통 예외 클래스
│
├── scripts/                         # 게임 로직 진입점
│   ├── command/                     # Lamp 명령어 핸들러
│   │   ├── PlayerCommand.java       # /player ...
│   │   ├── WorldCommand.java        # /world load|spawn|tp
│   │   └── AdminCommand.java        # /admin ...
│   └── event/                       # Minestom 이벤트 핸들러
│       ├── PlayerEventHandler.java  # Join / Quit / Respawn
│       ├── WorldEventHandler.java   # ChunkLoad / WeatherChange
│       └── CombatEventHandler.java  # 전투 관련 이벤트
│
└── system/                          # 독립적인 게임 시스템 단위 (도메인)
    │
    ├── player/                      # 플레이어 도메인
    │   ├── model/
    │   │   └── McPlayer.java        # 플레이어 데이터 (코인, 레벨, 통계)
    │   ├── PlayerRepository.java    # MongoDB CRUD
    │   ├── PlayerCache.java         # Redis Cache-Aside
    │   └── PlayerService.java       # 경험치, 레벨업 비즈니스 로직
    │
    ├── economy/                     # 경제 시스템
    │   ├── model/
    │   │   └── Transaction.java
    │   ├── EconomyRepository.java
    │   └── EconomyService.java      # 코인 지급, 거래, 세금
    │
    ├── world/                       # 월드 시스템 (Polar)
    │   ├── model/
    │   │   └── McWorld.java
    │   ├── WorldRepository.java
    │   └── WorldService.java        # 월드 로드, 스폰 설정
    │
    ├── combat/                      # 전투 시스템
    │   ├── model/
    │   │   └── CombatSession.java   # 전투 1회 상태
    │   └── CombatService.java       # 피해 계산, 콤보, 무적 판정
    │
    ├── npc/                         # NPC 시스템 (WorldSeedEntityEngine)
    │   ├── model/
    │   │   └── Npc.java
    │   └── NpcService.java          # 대화, 호감도, 스케줄
    │
    └── shop/                        # 상점 시스템
        ├── model/
        │   └── ShopItem.java
        ├── ShopService.java         # 구매, 판매, 재고 관리
        └── gui/
            └── ShopGui.java         # 인벤토리 GUI
```

---

## 레이어 설계 원칙

### `core/` — 인프라
- DB 연결, 설정 로딩 등 **게임 내용과 무관한** 인프라만 담습니다.
- 다른 패키지에서 `core`는 **참조**할 수 있지만, `core`는 `scripts/`·`system/`을 **참조하면 안 됩니다.**

### `scripts/` — 게임 이벤트 진입점
- **`command/`** : Lamp 어노테이션으로 명령어를 선언합니다. 비즈니스 로직은 직접 작성하지 않고 `system/**Service`에 위임합니다.
- **`event/`** : Minestom `EventNode`에 등록되는 리스너입니다. 마찬가지로 로직은 Service에 위임합니다.

### `system/` — 게임 도메인
- 각 시스템은 **model → Repository → Service** 3계층으로 구성됩니다.
- `Service`만 공개 API이며, `Repository`와 `model`은 같은 패키지 내에서만 접근을 권장합니다.
- 시스템 간 의존은 **Service → Service** 만 허용합니다 (Repository 직접 참조 금지).

---

## 서버 시작 순서

```
Main.main()
  └─ Server.start()
       ├─ initMinestom()      # MinecraftServer.init()
       ├─ connectDB()         # MongoConnect + RedisConnect
       ├─ registerCommands()  # Lamp → scripts/command/*
       ├─ registerEvents()    # EventNode → scripts/event/*
       └─ minecraftServer.start("0.0.0.0", 25565)
```

---

## 설정 파일

```
src/main/resources/
├── config.yml          # 서버 전반 설정 (포트, 메시지 등)
├── worlds.yml          # 로드할 월드 목록 및 스폰 좌표
└── shops/
    └── default.yml     # 상점 아이템 정의
```

환경 변수는 루트 `.env` 파일에서 로드됩니다 (`dotenv-java`).

```
MONGODB_URI=mongodb+srv://...
MONGODB_NAME=mcuniverse
REDIS_URI=redis://...
```
