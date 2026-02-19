# 시스템 아키텍처 (System Architecture)

## 1. 아키텍처 개요 (Overview)
본 프로젝트는 `noc_tr2_se8_park.pdf`에서 제시된 **One World Server Architecture** 개념을 차용하여, 대규모 동시 접속자를 처리할 수 있는 고성능 게임 서버를 목표로 합니다.
핵심 철학은 **Low Latency(낮은 지연시간)**, **High Concurrency(높은 동시성)**, **Scalability(확장성)** 입니다.

---

## 2. 핵심 설계 원칙 (Core Design Principles)

### 2.1. Stateless & Homogeneous Server (무상태 & 동질적 서버)
- **로직과 데이터의 분리**: 게임 로직을 처리하는 서버는 상태를 가지지 않습니다(Stateless). 모든 상태 데이터는 별도의 고속 저장소(In-Memory DB, Cache) 또는 DB 클러스터에 저장됩니다.
- **동질적 구성**: 모든 로직 서버는 동일한 기능을 수행할 수 있도록 구성하여, 특정 서버에 부하가 집중되는 것을 방지하고 수평적 확장(Scale-out)을 용이하게 합니다.

### 2.2. LMAX Disruptor 패턴 (Circular Buffer)
- **Lock-Free Concurrency**: 전통적인 멀티스레드 락(Lock) 방식의 오버헤드를 제거하기 위해 **Ring Buffer(Circular Buffer)** 를 사용합니다.
- **Single-Threaded Logic Workers**:
    - 요청(Event)은 Ring Buffer에 순차적으로 쌓입니다.
    - 비즈니스 로직을 처리하는 워커(Worker)는 **단일 스레드(Single Thread)** 로 동작하여 컨텍스트 스위칭 비용과 락 경합을 제거합니다.
    - 이를 통해 CPU 캐시 효율을 극대화하고 처리량을 비약적으로 높입니다.

### 2.3. 비동기 I/O 및 이벤트 소싱 (Async I/O & Event Sourcing)
- 모든 I/O 작업(DB 쓰기, 로깅 등)은 비동기로 처리하여 메인 로직 스레드, 블로킹되지 않도록 합니다.
- **Event Sourcing**: 상태 변경은 이벤트의 순차적 기록으로 관리되어, 장애 발생 시 이벤트를 리플레이(Replay)하여 상태를 복구할 수 있습니다.

---

## 3. 시스템 구성 요소 (Components)

### 3.1. Communication Layer (통신 계층)
- 클라이언트와의 연결을 담당합니다.
- 패킷을 수신하여 `Message Object`로 변환 후 Ring Buffer에 게시(Publish)합니다.
- **Netty** 등의 고성능 NIO 프레임워크 사용 권장.

### 3.2. Pre-Logic Processor (전처리 프로세서)
- 데이터 검증(Validation), 메시지 라우팅, 기본적인 필터링(예: 금칙어 처리)을 수행합니다.
- 멀티스레드로 동작하여 빠르게 유입되는 트래픽을 처리합니다.

### 3.3. Logic Processor (로직 프로세서 - Core)
- **Single-Threaded Consumer**: Ring Buffer에서 이벤트를 가져와 실제 게임 로직(이동, 전투, 거래 등)을 순차적으로 처리합니다.
- 인메모리(In-Memory) 상태를 참조하여 초고속으로 연산을 수행합니다.

### 3.4. Data Storage Layer (데이터 저장 계층)
- **Main DB (MySQL Cluster)**:
    - 핵심 게임 데이터(유저 정보, 인벤토리, 경제)를 저장합니다.
    - DB Clustering을 통해 쓰기/읽기 부하를 분산하고 고가용성을 확보합니다.
- **Log/History DB (NoSQL - Cassandra/MongoDB)**:
    - 대량의 로그 데이터(채팅, 행동 로그)를 저장합니다.
    - 쓰기 성능이 뛰어난 NoSQL을 채택합니다.
- **Cache (Redis)**:
    - 빈번하게 조회되는 데이터(세션, 랭킹, 실시간 정보)를 캐싱합니다.

---

## 4. 데이터 흐름 (Data Flow)

1. **Client Request**: 유저가 행동(예: 아이템 구매)을 요청합니다.
2. **Input Disruptor**: 통신 레이어에서 요청을 받아 Input Ring Buffer에 넣습니다.
3. **Logic Processing**: 로직 워커가 순차적으로 요청을 처리하고, 인메모리 상태를 갱신합니다.
4. **Log/Persistence**: 상태 변경 사항을 별도의 Output Buffer를 통해 DB/Log 서버로 비동기 전송합니다.
5. **Client Response**: 처리 결과를 클라이언트에게 응답합니다.

---

## 5. 기대 효과
- **동시 접속자 수용 능력 증대**: 단일 스레드 로직 처리 + 논블로킹 I/O를 통해 CPU 효율 극대화.
- **예측 가능한 지연 시간**: GC(Garbage Collection) 및 락 대기 시간 최소화로 안정적인 Latency 보장.
- **확장 용이성**: 서버 추가만으로 처리 용량을 늘릴 수 있는 구조.
