# 운영 매뉴얼 및 트러블슈팅 가이드

> **대상**: 서버 운영자, DevOps  
> **버전**: 1.0  
> **최종 업데이트**: 2026-02-17

---

## 목차

1. [[#서버 설정 및 배포]]
2. [[#일상 운영 작업]]
3. [[#트러블슈팅]]
4. [[#백업 및 복구]]
5. [[#성능 튜닝]]

---

## 서버 설정 및 배포

### 초기 설정

#### 1. 환경 변수 설정

```bash
# MongoDB URI (개인 섬 기능 사용 시)
export MONGODB_URI="mongodb://username:password@localhost:27017/mcuniverse"

# 선택적: 로그 레벨
export LOG_LEVEL="INFO"
```

**Windows (PowerShell)**:
```powershell
$env:MONGODB_URI = "mongodb://username:password@localhost:27017/mcuniverse"
```

> ⚠️ **MongoDB 미설정 시**: 개인 섬 기능만 비활성화되며, 나머지 기능은 정상 작동

#### 2. 디렉토리 구조 생성

```bash
mcuniverse/
├─ worlds/                  # Anvil 월드 (수동 배치)
│  ├─ lobby/
│  ├─ pvp_arena/
│  └─ event_world/
├─ worlds_polar/            # Polar 월드 (자동 생성)
│  ├─ rooms/               # 던전 방 템플릿
│  └─ templates/           # 변환된 월드
└─ worlds.yml              # 설정 파일 (자동 생성)
```

**디렉토리 자동 생성**:
- `worlds_polar/` - WorldFeature에서 자동 생성
- `worlds.yml` - 첫 실행 시 기본 템플릿 생성

#### 3. 의존성 체크

```bash
# Gradle 빌드
./gradlew build

# 의존성 확인
./gradlew dependencies | grep -E "polar|zstd|mongo"
```

**필수 의존성**:
- ✅ `net.hollowcube:polar:1.15.0`
- ✅ `com.github.luben:zstd-jni:1.5.7-7`
- ✅ `org.yaml:snakeyaml:2.2`

**선택 의존성**:
- ⚪ `org.mongodb:mongodb-driver-sync:4.11.0` (개인 섬용)

---

## 일상 운영 작업

### 월드 관리

#### 신규 월드 추가

```bash
# 1. Anvil 월드를 worlds/ 폴더에 복사
cp -r /path/to/new_world worlds/event_map

# 2. 서버 접속 후 로드
/world load event_map

# 3. 스폰 설정
# (원하는 위치로 이동 후)
/world setspawn event_map

# 4. 확인
/world list
```

**자동 적용 사항**:
- ✅ `worlds.yml`에 자동 추가
- ✅ 다음 재시작 시 자동 로드
- ✅ 스폰 좌표 자동 복원

#### 월드 업데이트

```bash
# 1. 기존 월드 백업
cp -r worlds/lobby worlds_backup/lobby_$(date +%Y%m%d)

# 2. 새 월드로 교체
cp -r /path/to/updated_lobby worlds/lobby

# 3. 서버에서 리로드
/world reload lobby

# 4. 검증
/world tp lobby
```

#### 월드 제거

```bash
# 1. 서버에서 언로드 (현재는 수동 재시작 필요)
# TODO: 향후 /world unload 명령어 추가 예정

# 2. worlds.yml 수정
nano worlds.yml
# autoload 목록에서 해당 월드 제거

# 3. 파일 삭제
rm -rf worlds/old_world

# 4. 서버 재시작
```

### Polar 템플릿 관리

#### Anvil → Polar 변환

```bash
# 서버 내에서 변환
/world import lobby lobby_template

# 결과: worlds_polar/templates/lobby_template.polar
```

**활용 사례**:
- 자주 사용하는 월드의 빠른 로딩
- 네트워크 전송용 경량 포맷
- 백업 공간 절약

#### 던전 방 템플릿 추가

```bash
# 1. 월드 제작 (WorldEdit 등)

# 2. Polar로 변환
/world import throne_room throne_room

# 3. 방 템플릿 폴더로 이동
mv worlds_polar/templates/throne_room.polar worlds_polar/rooms/

# 4. 메타데이터 작성
cat > worlds_polar/rooms/throne_room.json << 'EOF'
{
  "name": "throne_room",
  "size": {"width": 50, "height": 20, "depth": 50},
  "connectors": [
    {"side": "north", "position": {"x": 25, "y": 0, "z": 0}},
    {"side": "south", "position": {"x": 25, "y": 0, "z": 50}}
  ]
}
EOF
```

### 스폰 관리

#### 기본 스폰 월드 변경

```bash
# worlds.yml 수정
nano worlds.yml
```

```yaml
default_spawn_world: "new_lobby"  # 변경
```

```bash
# 서버 재시작
```

#### 월드별 스폰 좌표 확인

```bash
# worlds.yml 확인
cat worlds.yml | grep -A5 "name: lobby"
```

출력 예시:
```yaml
- name: "lobby"
  type: "anvil"
  spawn:
    x: 128.5
    y: 64.0
    z: 256.0
```

---

## 트러블슈팅

### 문제 1: 월드 로드 실패

**증상**:
```
§c월드 로드 실패: AnvilWorldImporter failed
```

**원인 및 해결**:

1. **Anvil 월드가 손상됨**
   ```bash
   # 백업에서 복구
   cp -r worlds_backup/lobby_20260215 worlds/lobby
   ```

2. **디스크 공간 부족**
   ```bash
   df -h
   # 불필요한 파일 삭제
   rm -rf worlds_polar/templates/*.old
   ```

3. **권한 문제**
   ```bash
   chmod -R 755 worlds/
   chown -R minecraft:minecraft worlds/
   ```

### 문제 2: 스폰이 설정되지 않음

**증상**:
```
/world setspawn lobby
§c설정 저장 실패: IOException
```

**원인 및 해결**:

1. **worlds.yml 권한 문제**
   ```bash
   chmod 644 worlds.yml
   ```

2. **YAML 포맷 오류**
   ```bash
   # 검증
   python -c "import yaml; yaml.safe_load(open('worlds.yml'))"
   
   # 오류 있으면 백업에서 복구
   cp worlds.yml.bak worlds.yml
   ```

### 문제 3: MongoDB 연결 실패

**증상**:
```
[ERROR] Failed to initialize MongoDB storage
java.net.UnknownHostException: mongodb
```

**원인 및 해결**:

1. **MongoDB 미실행**
   ```bash
   # Linux
   sudo systemctl start mongod
   
   # macOS
   brew services start mongodb-community
   
   # Windows
   net start MongoDB
   ```

2. **URI 오류**
   ```bash
   # 연결 테스트
   mongosh "$MONGODB_URI"
   
   # URI 형식 확인
   # mongodb://[username:password@]host[:port]/database
   ```

3. **네트워크 방화벽**
   ```bash
   # MongoDB 포트 확인 (기본 27017)
   telnet localhost 27017
   ```

### 문제 4: 메모리 부족

**증상**:
```
java.lang.OutOfMemoryError: Java heap space
```

**원인**: 너무 많은 월드 동시 로드

**해결**:

1. **즉시 조치**
   ```bash
   # 서버 재시작
   # worlds.yml에서 불필요한 월드 제거
   ```

2. **힙 메모리 증가**
   ```bash
   # start.sh 수정
   java -Xmx8G -Xms4G -jar server.jar
   #     ^^^^   ^^^^
   #     최대   최소
   ```

3. **월드 언로드 구현** (향후)
   ```java
   // TODO: 비활성 월드 자동 언로드
   if (inactiveTime > 10 * 60 * 1000) {
       worldRegistry.unload(worldName);
   }
   ```

### 문제 5: 플레이어가 기본 월드로 스폰됨

**증상**: `/world setspawn`으로 설정했지만 재접속 시 무시됨

**확인 사항**:

1. **worlds.yml 확인**
   ```bash
   cat worlds.yml | grep default_spawn_world
   ```

2. **스폰 좌표 확인**
   ```bash
   cat worlds.yml | grep -A6 "name: lobby"
   # spawn 섹션이 있는지 확인
   ```

3. **서버 로그 확인**
   ```bash
   grep "Auto-loaded world: lobby" logs/latest.log
   grep "Loaded world configuration" logs/latest.log
   ```

**해결**:
```bash
# 1. 스폰 재설정
/world tp lobby
/world setspawn lobby

# 2. worlds.yml 확인
cat worlds.yml

# 3. 서버 재시작
```

---

## 백업 및 복구

### 자동 백업 스크립트

```bash
#!/bin/bash
# backup_worlds.sh

BACKUP_DIR="/path/to/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# Anvil 월드 백업
tar -czf "$BACKUP_DIR/worlds_$DATE.tar.gz" worlds/

# Polar 템플릿 백업
tar -czf "$BACKUP_DIR/polar_$DATE.tar.gz" worlds_polar/

# 설정 파일 백업
cp worlds.yml "$BACKUP_DIR/worlds_$DATE.yml"

# 7일 이상 된 백업 삭제
find "$BACKUP_DIR" -name "*.tar.gz" -mtime +7 -delete

echo "Backup completed: $DATE"
```

**Cron 설정** (매일 03:00):
```bash
crontab -e
# 추가
0 3 * * * /path/to/backup_worlds.sh >> /var/log/world_backup.log 2>&1
```

### MongoDB 백업

```bash
#!/bin/bash
# backup_mongodb.sh

BACKUP_DIR="/path/to/mongo_backups"
DATE=$(date +%Y%m%d_%H%M%S)

# 전체 DB 백업
mongodump --uri="$MONGODB_URI" --out="$BACKUP_DIR/dump_$DATE"

# 압축
tar -czf "$BACKUP_DIR/mongo_$DATE.tar.gz" "$BACKUP_DIR/dump_$DATE"
rm -rf "$BACKUP_DIR/dump_$DATE"

# 30일 이상 된 백업 삭제
find "$BACKUP_DIR" -name "*.tar.gz" -mtime +30 -delete

echo "MongoDB backup completed: $DATE"
```

### 복구 절차

#### 월드 복구

```bash
# 1. 서버 중지
systemctl stop minecraft

# 2. 백업 해제
tar -xzf /path/to/backups/worlds_20260215.tar.gz -C /

# 3. 권한 복구
chown -R minecraft:minecraft worlds/

# 4. 서버 시작
systemctl start minecraft
```

#### MongoDB 복구

```bash
# 1. 백업 해제
tar -xzf /path/to/mongo_backups/mongo_20260215.tar.gz

# 2. 복구
mongorestore --uri="$MONGODB_URI" dump_20260215/

# 3. 확인
mongosh "$MONGODB_URI" --eval "db.islands.countDocuments()"
```

---

## 성능 튜닝

### JVM 최적화

```bash
# start.sh
java \
  -Xmx8G -Xms4G \                    # 힙 메모리
  -XX:+UseG1GC \                     # G1 가비지 컬렉터
  -XX:MaxGCPauseMillis=200 \         # 최대 GC 일시정지 시간
  -XX:+ParallelRefProcEnabled \      # 병렬 참조 처리
  -XX:+UnlockExperimentalVMOptions \ 
  -XX:+DisableExplicitGC \           # System.gc() 비활성화
  -XX:G1NewSizePercent=30 \
  -XX:G1MaxNewSizePercent=40 \
  -XX:G1HeapRegionSize=8M \
  -XX:G1ReservePercent=20 \
  -jar server.jar nogui
```

### MongoDB 최적화

#### 인덱스 생성

```javascript
use mcuniverse;

// 월드 이름 인덱스 (필수)
db.islands.createIndex({ "worldName": 1 }, { unique: true });

// 소유자 UUID 인덱스
db.islands.createIndex({ "metadata.ownerUuid": 1 });

// 최근 업데이트 순 조회용
db.islands.createIndex({ "updatedAt": -1 });

// 인덱스 확인
db.islands.getIndexes();
```

#### 압축 레벨 조정

```java
// MongoWorldStorage.java
// 압축: 레벨 낮추면 빠르지만 용량 증가
byte[] compressed = Zstd.compress(polarData, 3); // 기본: maxLevel()
```

**압축 레벨 성능**:

| 레벨 | 압축 시간 | 용량 절감 | 권장 용도 |
|------|----------|----------|----------|
| 1 | 10ms | 50% | 실시간 저장 |
| 3 | 30ms | 60% | 일반 저장 |
| 최고 | 150ms | 70% | 백업/아카이브 |

### 디스크 I/O 최적화

#### SSD 사용 권장

```bash
# 디스크 성능 테스트
sudo hdparm -Tt /dev/sda

# SSD 확인
lsblk -d -o name,rota
# rota=0: SSD
# rota=1: HDD
```

#### 파일시스템 최적화

```bash
# ext4 옵션 (SSD용)
sudo mount -o noatime,nodiratime,data=writeback /dev/sda1 /minecraft

# fstab 영구 설정
/dev/sda1  /minecraft  ext4  noatime,nodiratime,data=writeback  0  2
```

### 네트워크 최적화

#### MongoDB 연결 풀

```java
MongoClientSettings settings = MongoClientSettings.builder()
    .applyConnectionString(new ConnectionString(mongoUri))
    .applyToConnectionPoolSettings(builder -> 
        builder.maxSize(50)               // 최대 연결 수
               .minSize(10)               // 최소 연결 수
               .maxWaitTime(2, SECONDS)   // 연결 대기 시간
    )
    .build();
```

---

## 모니터링

### 로그 모니터링

```bash
# 실시간 로그 확인
tail -f logs/latest.log | grep -E "ERROR|WARN|WorldFeature"

# 월드 로딩 시간 분석
cat logs/latest.log | grep "Auto-loaded world" | \
  awk '{print $NF}' | sort | uniq -c
```

### 리소스 모니터링

```bash
# CPU/메모리 사용률
top -p $(pgrep -f minecraft)

# 디스크 사용량
du -sh worlds/ worlds_polar/

# MongoDB 상태
mongosh "$MONGODB_URI" --eval "db.serverStatus()"
```

### 알림 설정 (예시)

```bash
# check_memory.sh
#!/bin/bash

MEMORY_USAGE=$(ps aux | grep minecraft | awk '{print $4}')
THRESHOLD=80

if (( $(echo "$MEMORY_USAGE > $THRESHOLD" | bc -l) )); then
    echo "HIGH MEMORY: ${MEMORY_USAGE}%" | mail -s "Minecraft Alert" admin@example.com
fi
```

---

**문서 버전**: 1.0  
**최종 업데이트**: 2026-02-17  
**담당자**: DevOps팀
