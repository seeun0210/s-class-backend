# S-Class Backend Architecture

## 모듈 구조

```
s-class-backend/
├── SClass-Common          # 공통 코드 (exception, dto, annotation, util)
├── SClass-Domain          # 도메인 엔티티, 리포지토리, 도메인 서비스
├── SClass-Infrastructure  # 외부 연동 (Redis, S3, PubSub, Feign 등)
├── SClass-Api-Management  # 관리자/선생님 API (Web)
├── SClass-Api-Supporters  # 서포터즈 API (Web)
└── SClass-Batch           # 배치 처리
```

## 의존성 흐름

```
Api-Management ─┐
Api-Supporters ─┼→ Domain ──→ Common
Batch ──────────┘  Infrastructure ──→ Common
```

- **Common**: 어떤 모듈에도 의존하지 않음 (최하위)
- **Domain**: Common에 의존. JPA 엔티티, 리포지토리, 도메인 서비스 포함
- **Infrastructure**: Common에 의존. 외부 시스템 연동
- **Api-*/Batch**: Domain, Infrastructure, Common 모두에 의존 (최상위, bootJar 생성)

## 레이어 패턴

```
[Controller] → [UseCase] → [DomainService] → [Adaptor] → [Repository]
   Api 모듈      Api 모듈     Domain 모듈     Domain 모듈   Domain 모듈
```

| 레이어 | 위치 | 역할 | 어노테이션 |
|--------|------|------|-----------|
| Controller | Api 모듈 | HTTP 요청/응답 처리 | `@RestController` |
| UseCase | Api 모듈 | 애플리케이션 서비스, 유즈케이스 오케스트레이션 | `@UseCase` |
| DomainService | Domain 모듈 | 비즈니스 로직, 트랜잭션 관리 | `@DomainService` |
| Adaptor | Domain 모듈 | 데이터 접근 래퍼, 예외 변환 | `@Adaptor` |
| Repository | Domain 모듈 | JPA 리포지토리 인터페이스 | `JpaRepository` |

## 커스텀 어노테이션

| 어노테이션 | 용도 | 모듈 |
|-----------|------|------|
| `@UseCase` | 애플리케이션 서비스 (유즈케이스) | Api |
| `@DomainService` | 도메인 서비스 (비즈니스 로직) | Domain |
| `@Adaptor` | 인프라 어댑터 (데이터 접근) | Domain, Infrastructure |

모두 `@Component` 메타 어노테이션을 포함하여 Spring 빈으로 자동 등록됨.

## Exception 처리

### 구조

```
ErrorCode (interface)
├── GlobalErrorCode (enum)     # 공통 에러 (BAD_REQUEST, NOT_FOUND 등)
└── *ErrorCode (enum)          # 도메인별 에러 (StudentErrorCode, CourseErrorCode 등)

BusinessException (open class)
└── 도메인별 Exception          # ex) StudentNotFoundException
```

### ErrorCode 인터페이스

```kotlin
interface ErrorCode {
    val code: String        // "STUDENT_001"
    val message: String     // "학생을 찾을 수 없습니다"
    val httpStatus: Int     // 404
}
```

### 도메인별 ErrorCode 정의 예시

```kotlin
enum class StudentErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    STUDENT_NOT_FOUND("STUDENT_001", "학생을 찾을 수 없습니다", 404),
    STUDENT_ALREADY_EXISTS("STUDENT_002", "이미 존재하는 학생입니다", 409),
}
```

### 예외 던지기

```kotlin
throw BusinessException(StudentErrorCode.STUDENT_NOT_FOUND)
```

## API Response 형식

### 성공

```json
{
  "success": true,
  "data": { ... }
}
```

### 에러

```json
{
  "success": false,
  "error": {
    "code": "STUDENT_001",
    "message": "학생을 찾을 수 없습니다",
    "status": 404
  }
}
```

## 도메인 패키지 구조

Domain 모듈 내 각 도메인은 다음 구조를 따름:

```
domain/domains/{도메인명}/
├── domain/        # 엔티티, 값 객체 (JPA Entity)
├── repository/    # JpaRepository 인터페이스
├── adaptor/       # 데이터 접근 어댑터 (@Adaptor)
├── service/       # 도메인 서비스 (@DomainService)
└── exception/     # 도메인 에러코드, 예외 클래스
```

## 네이밍 컨벤션

| 항목 | 규칙 | 예시 |
|------|------|------|
| 엔티티 | `{Name}` | `Student`, `Course` |
| 리포지토리 | `{Name}Repository` | `StudentRepository` |
| 어댑터 | `{Name}Adaptor` | `StudentAdaptor` |
| 도메인 서비스 | `{Name}DomainService` | `StudentDomainService` |
| 유즈케이스 | `{동사}{Name}UseCase` | `ReadStudentUseCase`, `CreateCourseUseCase` |
| 컨트롤러 | `{Name}Controller` | `StudentController` |
| 에러코드 | `{Name}ErrorCode` | `StudentErrorCode` |
| ID (ULID) | 26자 String | `01ARZ3NDEKTSV4RRFFQ69G5FAV` |
