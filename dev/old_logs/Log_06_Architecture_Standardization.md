# 📅 DevLog: Architecture Standardization (Removing Factory Pattern)
#Architecture #Refactoring #YAGNI #MongoDB

## 📝 주제: 불필요한 추상화 제거 (Factory Pattern Removal)

> [!INFO] **요약**
> 프로젝트의 저장소 기술을 **MongoDB로 표준화**함에 따라, 다형성을 위해 존재했던 **Factory Pattern**을 제거했습니다.
> "혹시 몰라(Just in case)" 만들어둔 코드를 삭제하고, **"지금 필요한(You Aren't Gonna Need It)"** 코드에 집중하여 복잡도를 낮췄습니다.

---

### 1. 🔄 Context: 과거의 설계의도
[[Log_01_Economy_System]]에서 우리는 **`EconomyFactory`**를 도입했습니다.
당시에는 저장소 기술이 확정되지 않았거나, 로컬 테스트용 `MEMORY` 모드와 배포용 `MONGODB` 모드를 오가야 할 가능성을 열어두고 싶었기 때문입니다.

```java
// Before: 유연하지만 복잡함
EconomyStrategy strategy = EconomyFactory.createStrategy(type); // type이 뭘까? 런타임에 결정됨
```

### 2. ⚠️ Problem: 과도한 추상화 비용
하지만 개발이 진행되면서(`Log_05` 시점), MongoDB에 의존적인 기능들(복합 인덱스, TTL 등)이 적극적으로 도입되었습니다.
- **사실상 고정된 구현체**: 실제 운영 환경에서는 100% `MongoEconomyStrategy`만 쓰게 되었습니다.
- **불필요한 코드**: `RankFactory`, `EconomyFactory` 등 단순히 객체를 생성(`new`)해주는 역할만 하는 클래스들이 늘어났습니다.
- **버그 위험**: `RankFeature` 구현 과정에서 Factory 호출 로직이 복잡하게 꼬여, 초기화가 제대로 안 되는 실수도 발견되었습니다.

### 3. 💡 Solution: YAGNI 원칙 적용
**"You Aren't Gonna Need It" (그건 필요 없을 거야)** 원칙을 적용하여, 당장 불필요한 유연함을 제거하고 코드를 **명시적**으로 변경했습니다.

- **Action**: `EconomyFactory`, `RankFactory` 클래스 삭제.
- **Action**: Feature 클래스에서 직접 구현체(`MongoStrategy`) 주입.

```java
// After: 단순하고 명확함 (Standardization)
EconomyStrategy strategy = new MongoEconomyStrategy(); // 우린 몽고디비만 쓴다!
```

### 4. 📈 Benefits
1. **코드 가독성 향상**: `EconomyFeature`만 봐도 "아, 이 서버는 MongoDB를 쓰는구나"라고 바로 알 수 있습니다.
2. **유지보수 용이**: 팩토리 클래스를 타고 들어가는 뎁스(Depth)가 줄어들어 디버깅이 편해졌습니다.
3. **기능 확장성**: MongoDB 특화 기능(예: Aggregation Pipeline 등)을 인터페이스 뒤에 숨기지 않고 더 과감하게 쓸 수 있는 기반이 되었습니다. (물론 `Strategy` 인터페이스는 유지하여 테스트 가능성은 남겨두었습니다.)

### 5. 🔗 Connected Logs
- [[Log_01_Economy_System]]: Factory 패턴이 처음 도입되었던 시점.
- [[Log_05_Economy_Schema_Improvement]]: MongoDB 특화 기능인 TTL과 Schema가 도입되면서, Factory 제거의 필요성이 대두됨.
