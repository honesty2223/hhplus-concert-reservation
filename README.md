# Query 분석 및 인덱싱을 통한 조회 쿼리 개선
<details>
<summary><b>테스트 데이터 INSERT</b></summary>

- 멀티스레드를 이용한 빠른 적재

```java
    @Test
    @DisplayName("좌석 200만 건 저장")
    public void insertSeat() throws InterruptedException {
        int lastNum = 0;
        int finishedJ = 0;
        for(int i = 1; i <= 20; i++) {
            for(int j = 1; j <= 100000; j+=5) {
                Thread thread1 = new Thread(new MultiThread(j + lastNum, i, j, seatService));
                Thread thread2 = new Thread(new MultiThread(j + lastNum + 1, i, j, seatService));
                Thread thread3 = new Thread(new MultiThread(j + lastNum + 2, i, j, seatService));
                Thread thread4 = new Thread(new MultiThread(j + lastNum + 3, i, j, seatService));
                Thread thread5 = new Thread(new MultiThread(j + lastNum + 4, i, j, seatService));
                thread1.start();
                thread2.start();
                thread3.start();
                thread4.start();
                thread5.start();
                finishedJ = j + lastNum;
            }
            lastNum = lastNum + finishedJ;
        }
    }
```
</details>

<details>
<summary><b>분석</b></summary>

### 예약 가능한 콘서트 좌석 정보를 담고 있는 `Seat` 테이블에 인덱싱 적용

1. **예약 가능한 콘서트 좌석 조회**: 콘서트 스케줄 ID를 기반으로 예약 가능한 좌석을 조회

### Query 분석

1. **`findAvailableSeats` 쿼리**:
- **입력**: `concertScheduleId`
- **동작**: 콘서트 스케줄 ID에 해당하는 좌석을 조회
- **문제점**: 한 콘서트 당 좌석이 10만 개 정도 있다고 가정하면, 대량의 트래픽이 발생할 경우 최소 몇 백만 건의 데이터베이스 조회로 인한 성능 저하 발생 가능

### 인덱스 적용

예약 가능한 콘서트 일정 조회 시 대량의 트래픽이 발생할 경우 최소 몇 백만 건의 데이터베이스 조회로 인해 성능 저하가 발생할 수 있다고 판단, 이를 방지하기 위해 Seat 테이블에 concert_scheduled_id 컬럼을 인덱스로 생성하여 DB의 부하를 덜어 주기로 결정

### 인덱스 설명

1. **인덱스 생성**:
```sql
-- SQL문으로 인덱스를 만들 경우
CREATE INDEX idx_concert_schedule_id ON SEAT (concert_schedule_id);
```
```java
// SprintBoot JPA로 생성할 경우
@Table(name = "seat", indexes = { @Index(name = "idx_concert_schedule_id_and_finally_reserved", columnList = "concert_schedule_id") })
```

2. **인덱스 조회**:
```sql
-- SQL문으로 인덱스 조회
SHOW INDEX FROM SEAT;
```
![index](https://github.com/user-attachments/assets/d27f2a00-176c-4cb9-b48c-c2ac83d20245)

### 적용 후 기대 효과

- **검색 성능 향상**: 전체 테이블을 스캔하는 대신 인덱스(idx_concert_schedule_id)를 참조하여 원하는 데이터를 검색.

</details>

<details>
<summary><b>인덱스 적용 테스트 결과</b></summary>

### `DBeaver를 통한 결과 확인`

- 쿼리의 실행시간을 볼 수 있도록 Profile 생성
```sql
SET profiling = 1;
```
- 인덱스 없이 Seat 테이블 조회
```sql
SELECT *
FROM SEAT
WHERE concert_schedule_id = 1
AND finally_reserved = false
AND (temp_assignee_id IS NULL OR temp_assignee_id = 0);
```
- 인덱스 생성 전 쿼리 EXPLAIN 확인
```sql
EXPLAIN
SELECT *
FROM SEAT
WHERE concert_schedule_id = 1
AND finally_reserved = false
AND (temp_assignee_id IS NULL OR temp_assignee_id = 0);
```
![explain_without_index](https://github.com/user-attachments/assets/a182ae52-05ec-45cd-9a64-8240fead6899)
- 인덱스 생성 전 쿼리 실행시간 확인
```sql
SHOW PROFILES
```
![query_without_index](https://github.com/user-attachments/assets/1421047e-7bf4-4cf0-9e9b-5897e9aa51c0)
- 인덱스 생성
```sql
CREATE INDEX idx_concert_schedule_id ON SEAT (concert_schedule_id);
```
- 인덱스 확인
```sql
SHOW INDEX FROM SEAT;
```
![index_info](https://github.com/user-attachments/assets/40715ea8-d2bb-42e9-879e-4a4778c18479)
- 인덱스 생성 후 쿼리 EXPLAIN 확인
```sql
EXPLAIN
SELECT *
FROM SEAT
WHERE concert_schedule_id = 1
AND finally_reserved = false
AND (temp_assignee_id IS NULL OR temp_assignee_id = 0);
```
![explain_with_index](https://github.com/user-attachments/assets/12ca0c31-b98d-4fb7-b090-c0d0079e8e54)
- 인덱스 생성 후 쿼리 실행시간 확인
```sql
SHOW PROFILES
```
![query_with_index](https://github.com/user-attachments/assets/272b0fab-e352-462f-aca0-4edcc824ecea)
### `테스트 결과`
- Explain에서 Type이 ref인 것을 확인하여 Index를 통한 조회가 잘 되었음을 알 수 있다.
- 쿼리 조회 시간이 0.36초 -> 0.09초로 향상되었다.
- 총 데이터 200만 건에서 10만 건을 조회한 경우라 0초대가 나오긴 했지만, 그래도 대량의 트래픽이 발생할 경우에는 1초가 걸리는 쿼리도 리스크가 있으므로 유의미한 쿼리 개선 효과라고 생각한다.
</details>

# MSA 서비스 분리 및 트랜잭션 처리 해결방안
<details>
<summary><b>분석</b></summary>

### Choreography 패턴
각 서비스가 자신의 트랜잭션과 관련된 이벤트를 발행하고, 다른 서비스는 이 이벤트를 구독하여 필요한 후속 작업을 수행

### 결과 및 기대 효과

1. **서비스 독립성 유지**:
- **Choreography** 패턴을 사용하여 각 서비스가 자신의 트랜잭션을 독립적으로 처리하고, 이벤트를 발행하여 다른 서비스와 통신함. 이로 인해 서비스 간의 결합도가 낮아지며, 서비스는 서로 독립적으로 배포 및 확장할 수 있음

2. **비즈니스 로직의 흐름 관리**:
- 이벤트를 발행하고 구독하는 방식으로 비즈니스 로직의 흐름을 자연스럽게 관리할 수 있음

3. **트랜잭션의 복잡성 감소**:
- 각 서비스가 자신의 트랜잭션을 관리하고, 실패 시에는 관련된 이벤트를 발행하여 롤백 작업을 수행함. 이로 인해 분산 시스템에서 트랜잭션의 복잡성을 줄일 수 있음

4. **비동기 처리**:
- 이벤트 기반의 처리 방식은 비동기적으로 작업을 수행할 수 있음. 이는 높은 성능과 확장성을 제공하며, 서비스가 서로 독립적으로 작업을 수행할 수 있음

5. **트랜잭션 관리**:
- **좌석 서비스**, **예약 서비스**, **결제 서비스**, **토큰 서비스** 각각의 서비스는 자신의 트랜잭션을 처리하며, 실패 시 적절한 롤백 이벤트를 발행
  - **좌석 서비스**에서 좌석 예약이 실패하면 `SeatReservationFailedEvent`를 발행
  - **예약 서비스**는 예약 생성 실패 시 `ReservationCreationFailedEvent`를 발행하고, 이 이벤트를 수신한 다른 서비스는 필요한 롤백 작업을 수행
  - **결제 서비스**와 **토큰 서비스**는 결제 및 토큰 만료 처리 실패 시 각각 `PaymentProcessingFailedEvent`와 `TokenExpirationFailedEvent`를 발행하여 관련 서비스를 롤백


### 서비스 분리
1. **좌석 서비스 (Seat Service)**
- **역할**: 좌석 예약 및 상태 업데이트
- **트랜잭션**: 좌석 상태를 예약된 상태로 업데이트
- **이벤트**: `SeatReservedEvent` 발행

2. **예약 서비스 (Reservation Service)**
- **역할**: 예약 생성 및 상태 업데이트
- **트랜잭션**: 예약 정보를 데이터베이스에 저장
- **이벤트**: `ReservationCreatedEvent` 발행

3. **결제 서비스 (Payment Service)**
- **작업**: 결제 내역 생성 및 결제 처리
- **트랜잭션**: 결제 내역을 데이터베이스에 저장하고, 결제 금액을 차감
- **이벤트**: `PaymentProcessedEvent` 발행

4. **토큰 서비스 (Token Service)**
- **작업**: 토큰 만료 처리
- **트랜잭션**: 토큰을 만료 처리
- **이벤트**: `TokenExpiredEvent` 발행

### 이벤트 흐름

1. **좌석 예약 생성**
- 좌석 서비스가 좌석 예약 요청을 받고, 좌석 상태를 예약된 상태로 업데이트
- 좌석 예약이 성공적으로 완료되면 `SeatReservedEvent`를 발행

2. **예약 생성**
- 예약 서비스는 `SeatReservedEvent`를 구독하여 예약을 생성
- 예약 생성 후 `ReservationCreatedEvent`를 발행

3. **결제 처리**
- 결제 서비스는 `ReservationCreatedEvent`를 구독하여 결제를 처리
- 결제 완료 후 `PaymentProcessedEvent`를 발행

4. **토큰 만료 처리**
- 토큰 서비스는 `PaymentProcessedEvent`를 구독하여 토큰 만료 처리

## 서비스 구현 및 보상 트랜잭션

**좌석 서비스**

```java
public class SeatService {
  @Autowired
  private EventPublisher eventPublisher;

  @Transactional
  public void reserveSeat(long seatId, long customerId) {
    try {
      // 좌석 예약 로직 수행
      eventPublisher.publish(new SeatReservedEvent(seatId, customerId));
    } catch(Exception e) {
      // 좌석 예약 실패 시 롤백 없음
      eventPublisher.publish(new SeatReservationFailedEvent(seatId, customerId));
      throw e;
    }
  }
}
```

**예약 서비스**

```java
public class ReservationService {
  @Autowired
  private EventPublisher eventPublisher;

  @Transactional
  public void createReservation(long seatId, long customerId) {
    try {
      // 예약 생성 로직 수행
      eventPublisher.publish(new ReservationCreatedEvent(seatId, customerId));
    } catch(Exception e) {
      // 예약 생성 실패 시 좌석 롤백
      eventPublisher.publish(new ReservationCreationFailedEvent(seatId, customerId));
      throw e;
    }
  }
}
```

**결제 서비스**

```java
public class PaymentService {
  @Autowired
  private EventPublisher eventPublisher;

  @Transactional
  public void processPayment(long reservationId, long amount) {
    try {
      // 결제 처리 로직 수행
      eventPublisher.publish(new PaymentProcessedEvent(reservationId, amount));
    } catch(Exception e) {
      // 결제 처리 실패 시 예약 롤백 및 좌석 롤백
      eventPublisher.publish(new PaymentProcessingFailedEvent(reservationId, amount));
      throw e;
    }
  }
}
```

**토큰 서비스**

```java
public class TokenService {
  @Autowired
  private EventPublisher eventPublisher;

  @Transactional
  public void expireToken(long customerId) {
    try {
      // 토큰 만료 처리 로직 수행
      eventPublisher.publish(new TokenExpiredEvent(customerId));
    } catch(Exception e) {
      // 토큰 만료 처리 실패 시 결제 및 예약, 좌석 롤백
      eventPublisher.publish(new TokenExpirationFailedEvent(customerId));
      throw e;
    }
  }
}
```

</details>

# Query 분석 및 캐싱 전략 설계
<details>
<summary><b>분석</b></summary>
콘서트 일정 조회와 저장에 관련된 기능을 제공하는 `ConcertScheduleService`에 캐싱 적용

1. **콘서트 일정 조회**: 콘서트 ID를 기반으로 콘서트 일정을 조회
2. **콘서트 일정 저장**: 새로운 콘서트 일정을 저장하고 캐시를 갱신하는 기능

### Query 분석

1. **`findByConcertId` 쿼리**:
  - **입력**: `concertId`
  - **동작**: 콘서트 ID에 해당하는 콘서트 일정을 조회
  - **문제점**: 대량의 트래픽이 발생할 경우 데이터베이스 조회로 인한 성능 저하 발생 가능

2. **`save` 쿼리**:
  - **입력**: `concertSchedule` 객체
  - **동작**: 새로운 콘서트 일정을 데이터베이스에 저장하고 캐시를 갱신
  - **문제점**: 저장 후 캐시를 갱신하지 않으면 일관성 문제가 발생할 수 있음


### 캐싱 적용

콘서트 일정 조회 시 대량의 트래픽이 발생할 경우 데이터베이스 조회로 인해 성능 저하가 발생할 수 있다고 판단, 이를 방지하기 위해 조회 결과를 캐싱하여 자주 조회되는 데이터를 메모리에서 빠르게 제공하기로 결정

### 캐싱 설명

1. **캐시 키 설계**:
  - `concertSchedule::concertId` 형태로 캐시 키를 설계하여 특정 콘서트 ID에 대한 일정을 캐싱

2. **캐시 조회**:
  - `findByConcertId` 메서드에서 먼저 Redis 캐시를 조회하여 캐시에 데이터가 존재하면 캐시된 데이터를 반환하고, 존재하지 않으면 데이터베이스에서 조회한 후 캐시에 저장

3. **캐시 갱신**:
  - `save` 메서드에서 새로운 콘서트 일정을 저장한 후 해당 콘서트 ID에 대한 캐시를 갱신

4. **캐시 만료 시간**:
  - 캐시의 만료 시간을 10분으로 설정하여 일정 시간마다 캐시를 갱신하고 데이터 일관성을 유지

### 적용 후 기대 효과

- **조회 성능 향상**: 캐시를 활용하여 자주 조회되는 데이터를 메모리에서 빠르게 제공함으로써 데이터베이스 부하를 줄이고 응답 속도를 향상
- **트래픽 분산**: 대량의 트래픽이 발생할 때 캐시를 통해 데이터베이스로의 쿼리 요청을 분산시켜 성능 저하를 방지
- **데이터 일관성 유지**: 캐시 갱신 로직을 통해 데이터베이스와 캐시 간의 데이터 일관성을 유지

</details>



# 낙관적 락, 비관적 락 동시성 제어 성능 비교
<details>
<summary><b>동시성 이슈 및 제어 방식</b></summary>

### 1. 발생할 수 있는 동시성 이슈

- **좌석 예약 요청**: 여러 사용자가 동시에 동일한 좌석을 예약하려고 시도할 때 발생하는 충돌.
- **결제 처리 및 결제 내역 생성**: 따닥(?)과 같은 동일한 예약 건에 대해 결제를 시도할 때 발생하는 충돌.

### 2. 동시성 제어 방식 및 장단점

#### 비관적 락 (Pessimistic Lock)

**비관적 락**은 데이터에 접근할 때마다 락을 걸어 충돌을 방지하는 방식, 주로 충돌이 자주 발생하는 환경에서 데이터의 일관성을 보장할 때 사용

**장점**:
- **강력한 데이터 일관성 보장**: 모든 데이터에 락을 걸어 충돌을 방지하므로 데이터의 일관성을 강하게 보장
- **충돌 발생 시 안정적인 처리**: 충돌이 발생하지 않도록 미리 락을 걸어 안정적으로 처리

**단점**:
- **성능 저하 가능성**: 락을 걸고 해제하는 과정에서 성능이 저하될 가능성 있음
- **높은 리소스 소모**: 락을 유지하는 동안 리소스를 많이 소비
- **교착 상태 발생 위험**: 여러 스레드가 서로 다른 리소스를 잠그고, 다른 스레드가 소유한 리소스를 기다리면서 교착 상태가 발생할 가능성 있음

#### 낙관적 락 (Optimistic Lock)

**낙관적 락**은 데이터 충돌이 드물다고 가정하고 데이터 갱신 시 충돌을 감지하는 방식, 충돌이 발생하면 롤백하고 다시 시도

**장점**:
- **높은 성능**: 락을 거의 걸지 않으므로 성능이 높음
- **낮은 리소스 소비**: 락을 유지하지 않으므로 리소스 소비가 적음
- **교착 상태 방지**: 충돌이 발생했을 때 롤백하고 재시도하므로 교착 상태를 방지할 수 있음

**단점**:
- **충돌 발생 시 오버헤드**: 충돌이 발생하면 롤백과 재시도로 인해 오버헤드가 발생할 가능성 있음
- **일관성 보장이 상대적으로 약함**: 충돌이 발생할 가능성이 높은 경우 데이터의 일관성 보장이 상대적으로 약함

### 3. 구현의 복잡도, 성능, 효율성 비교

#### 비관적 락 (Pessimistic Lock)
- **구현의 복잡도**: 상대적으로 간단. 락을 걸고 해제하는 코드가 명확함
- **성능**: 동시성 충돌이 자주 발생하는 환경에서는 안정적이지만, 락으로 인한 성능 저하가 발생할 수 있음
- **효율성**: 리소스 소모가 높고, 교착 상태 발생 위험이 있음

#### 낙관적 락 (Optimistic Lock)
- **구현의 복잡도**: 복잡도가 증가할 수 있음. 충돌 감지와 재시도 로직을 추가해야 함
- **성능**: 대부분의 경우 높은 성능을 유지. 충돌 발생 시 재시도로 인한 오버헤드가 있을 수 있음
- **효율성**: 리소스 소비가 적고, 교착 상태를 방지할 수 있음

### 4. 선택한 동시성 제어 방식

#### `좌석 예약 요청`

  - **낙관적 락 선택**: 비관적 락을 사용하면 많은 사용자가 동시에 좌석을 예약하려고 할 때 교착 상태와 성능 저하가 발생할 것이라 판단, 낙관적 락은 충돌이 발생했을 때만 롤백하고 재시도하기 때문에 사용자의 동시 접근에 대한 영향을 최소화하고 자원 소비 측면에서도 효율적이지 않을까...

***

#### `결제 처리 및 결제 내역 생성`

  - **낙관적 락 선택**: 결제 처리의 경우 동일한 예약에 대해 중복 결제가 발생할 가능성이 낮다고 판단. 동시성 충돌이 드물고, 낙관적 락을 적용하면 데이터 충돌이 발생할 때만 롤백하고 재시도하기 때문에 처리 속도가 빠르고 성능이 우수하지 않을까...

</details>

<details>
<summary><b>낙관적 Lock 테스트 결과</b></summary>

### `좌석 예약 요청`

  - 통합 테스트 실행 시간
![좌석예약요청_낙관적_소요시간](https://github.com/user-attachments/assets/079fa477-99f0-4d23-addd-9fc11e5c34c2)

  - 테스트 결과
![좌석예약요청_낙관적_로그](https://github.com/user-attachments/assets/e2d896a3-4dcd-4bb9-995e-3857714d0950)

***

### `결제 처리 및 결제 내역 생성`

  - 통합 테스트 실행 시간
![결제_낙관적_소요시간](https://github.com/user-attachments/assets/39534b06-f8dc-4f0d-b216-92d714f7fe0b)

  - 테스트 결과
![결제_낙관적_로그1](https://github.com/user-attachments/assets/22c51233-8ce4-4eea-8300-63fbce858a85)
![결제_낙관적_로그2](https://github.com/user-attachments/assets/86bb7d13-daf4-4835-af2f-c55fab4451a2)

</details>

<details>
<summary><b>비관적 Lock 테스트 결과</b></summary>

### `좌석 예약 요청`

  - 소요시간
![좌석예약요청_비관적_소요시간](https://github.com/user-attachments/assets/a7223c78-1c07-4117-8700-38bc8073cf68)

  - 테스트 결과
![좌석예약요청_비관적_로그](https://github.com/user-attachments/assets/e8e2d6d5-0e45-4958-84ae-a52f8a284142)

***

### `결제 처리 및 결제 내역 생성`

  - 소요시간
![결제_비관적_소요시간](https://github.com/user-attachments/assets/268f08c6-ef75-45e9-a119-feba63525a13)

  - 테스트 결과
![결제_비관적_로그](https://github.com/user-attachments/assets/727a1da7-487b-4003-beee-c591881c4d65)

</details>


# 산출물

## [MileStone](https://github.com/users/honesty2223/projects/12)
![MileStone](https://github.com/honesty2223/hhplus-concert-reservation/assets/165884218/b19b47d1-66c8-4008-8794-ed31bcf876f5)

## [ERD](https://dbdiagram.io/d/hhplus-concert-reservation-6686e1ed9939893dae138e70)
![ERD](https://github.com/honesty2223/hhplus-concert-reservation/assets/165884218/8d3891a9-161c-475c-b0a3-5b028c7bc8d2)

## [API 명세서](https://documenter.getpostman.com/view/36570181/2sA3dyhqYp)
![API](https://github.com/honesty2223/hhplus-concert-reservation/assets/165884218/ec2bed66-bbdb-485b-bfc0-59e5c389d960)

## Sequence Diagram
![Sequence Diagram](https://github.com/honesty2223/hhplus-concert-reservation/assets/165884218/c451f225-7d33-409b-b6d8-a54af90055e5)

# [ 3~5주차 과제 ] 콘서트 예약 서비스
<aside>
💡 아래 명세를 잘 읽어보고, 서버를 구현합니다.

</aside>

## Description

- **`콘서트 예약 서비스`**를 구현해 봅니다.
- 대기열 시스템을 구축하고, 예약 서비스는 작업가능한 유저만 수행할 수 있도록 해야합니다.
- 사용자는 좌석예약 시에 미리 충전한 잔액을 이용합니다.
- 좌석 예약 요청시에, 결제가 이루어지지 않더라도 일정 시간동안 다른 유저가 해당 좌석에 접근할 수 없도록 합니다.

## Requirements

- 아래 5가지 API 를 구현합니다.
    - 유저 토큰 발급 API
    - 예약 가능 날짜 / 좌석 API
    - 좌석 예약 요청 API
    - 잔액 충전 / 조회 API
    - 결제 API
- 각 기능 및 제약사항에 대해 단위 테스트를 반드시 하나 이상 작성하도록 합니다.
- 다수의 인스턴스로 어플리케이션이 동작하더라도 기능에 문제가 없도록 작성하도록 합니다.
- 동시성 이슈를 고려하여 구현합니다.
- 대기열 개념을 고려해 구현합니다.

## API Specs

1️⃣ **`주요` 유저 대기열 토큰 기능**

- 서비스를 이용할 토큰을 발급받는 API를 작성합니다.
- 토큰은 유저의 UUID 와 해당 유저의 대기열을 관리할 수 있는 정보 ( 대기 순서 or 잔여 시간 등 ) 를 포함합니다.
- 이후 모든 API 는 위 토큰을 이용해 대기열 검증을 통과해야 이용 가능합니다.

> 기본적으로 폴링으로 본인의 대기열을 확인한다고 가정하며, 다른 방안 또한 고려해보고 구현해 볼 수 있습니다.
> 

**2️⃣ `기본` 예약 가능 날짜 / 좌석 API**

- 예약가능한 날짜와 해당 날짜의 좌석을 조회하는 API 를 각각 작성합니다.
- 예약 가능한 날짜 목록을 조회할 수 있습니다.
- 날짜 정보를 입력받아 예약가능한 좌석정보를 조회할 수 있습니다.

> 좌석 정보는 1 ~ 50 까지의 좌석번호로 관리됩니다.
> 

3️⃣ **`주요` 좌석 예약 요청 API**

- 날짜와 좌석 정보를 입력받아 좌석을 예약 처리하는 API 를 작성합니다.
- 좌석 예약과 동시에 해당 좌석은 그 유저에게 약 5분간 임시 배정됩니다. ( 시간은 정책에 따라 자율적으로 정의합니다. )
- 만약 배정 시간 내에 결제가 완료되지 않는다면 좌석에 대한 임시 배정은 해제되어야 하며 다른 사용자는 예약할 수 없어야 한다.

4️⃣ **`기본`**  **잔액 충전 / 조회 API**

- 결제에 사용될 금액을 API 를 통해 충전하는 API 를 작성합니다.
- 사용자 식별자 및 충전할 금액을 받아 잔액을 충전합니다.
- 사용자 식별자를 통해 해당 사용자의 잔액을 조회합니다.

5️⃣ **`주요` 결제 API**

- 결제 처리하고 결제 내역을 생성하는 API 를 작성합니다.
- 결제가 완료되면 해당 좌석의 소유권을 유저에게 배정하고 대기열 토큰을 만료시킵니다.

<aside>
💡 **KEY POINT**

</aside>

- 유저간 대기열을 요청 순서대로 정확하게 제공할 방법을 고민해 봅니다.
- 동시에 여러 사용자가 예약 요청을 했을 때, 좌석이 중복으로 배정 가능하지 않도록 합니다.
