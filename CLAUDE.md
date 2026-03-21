# S-Class Backend Convention Guide

## Tech Stack
- Spring Boot 4.0.3, Kotlin 2.1.10, Java 21
- MySQL, JPA + QueryDSL, ktlint 1.5.0
- ULID (26자) ID, BaseTimeEntity (JPA Auditing)

## Module Structure

```
SClass-Common        # 공통 (어노테이션, DTO, 예외, JWT, 유틸)
SClass-Domain        # 도메인 (엔티티, 리포지토리, 어댑터, 도메인서비스)
SClass-Infrastructure # 외부 연동 (S3, GCS, OAuth 등)
SClass-Api-Supporters  # 학생/학부모용 API
SClass-Api-Lms         # LMS API (수업/탐구/정산, Organization 종속)
SClass-Api-Backoffice  # 슈퍼어드민 Backoffice API
SClass-Batch         # 배치 처리
```

## Module Dependencies

```
Api-Supporters / Api-Lms / Api-Backoffice / Batch
    ↓
Domain + Infrastructure + Common
    ↓
Common (최하위)
```

- Api 모듈은 Domain, Infrastructure, Common 모두 의존
- Domain은 Common만 의존
- Infrastructure는 Common만 의존
- Common은 외부 의존 없음

## Layer Architecture

```
Controller (@RestController)  ← Api 모듈
    ↓
UseCase (@UseCase)            ← Api 모듈
    ↓
DomainService (@DomainService) ← Domain 모듈 (비즈니스 로직, 필요 시만)
    ↓
Adaptor (@Adaptor)            ← Domain 모듈 (읽기 + 쓰기)
    ↓
Repository (JpaRepository)    ← Domain 모듈
```

## Annotations

| 어노테이션 | 위치 | 역할 |
|-----------|------|------|
| `@UseCase` | Api 모듈 | 유즈케이스 오케스트레이션. DomainService/Adaptor 조합 |
| `@DomainService` | Domain 모듈 | 비즈니스 로직 (검증, 상태변경). 단순 CRUD면 생략 |
| `@Adaptor` | Domain 모듈 | Repository 래핑. 읽기+쓰기 모두 담당 |
| `@RestController` | Api 모듈 | HTTP 엔드포인트 |

## Domain Module Package Structure

```
domains/{feature}/
├── domain/       # Entity, Enum, VO
├── repository/   # JpaRepository 인터페이스 + CustomRepository
├── adaptor/      # @Adaptor (Repository 래핑, 예외 변환)
├── service/      # @DomainService (비즈니스 로직, 필요 시만)
├── exception/    # ErrorCode enum + BusinessException
├── dto/          # 도메인 계층 DTO (서비스 반환용)
└── config/       # 도메인별 설정 (필요 시)
```

## Api Module Package Structure

```
{module}/
└── {feature}/
    ├── controller/  # @RestController
    ├── dto/         # Request/Response DTO
    └── usecase/     # @UseCase
```

## Coding Conventions

### Entity
- ULID id (VARCHAR 26), `BaseTimeEntity` 상속
- `companion object { fun create(...) }` 팩토리 (필요 시)
- 컬럼 간 빈 줄 허용 (`.editorconfig`에서 `no-blank-line-in-list` 비활성)

### Adaptor
- 읽기 + 쓰기 모두 담당 (find, save, delete)
- 조회 실패 시 도메인 예외 throw: `throw UserNotFoundException()`
- `findByXxxOrNull` 패턴으로 nullable 조회 제공

### DomainService
- 비즈니스 로직이 있을 때만 생성 (단순 CRUD는 Adaptor로 충분)
- Adaptor를 주입받아 사용 (Repository 직접 참조 금지)
- 같은 도메인 내 다른 Service 참조 가능
- 도메인 엔티티를 반환 (DTO 아님)

### UseCase
- Api 모듈에 위치
- DomainService/Adaptor를 조합하여 유즈케이스 구현
- `@Transactional` 선언 (주 트랜잭션 경계)
- Request DTO → 도메인 호출 → Response DTO 반환

### Exception
```kotlin
// ErrorCode enum
enum class UserErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    USER_NOT_FOUND("USER_001", "유저를 찾을 수 없습니다", 404),
}

// Exception class (매번 새 인스턴스 생성 — 정확한 스택트레이스 확보)
class UserNotFoundException : BusinessException(UserErrorCode.USER_NOT_FOUND)
```

### API Response
- 모든 API는 `ApiResponse<T>` 래핑
- 성공: `ApiResponse.success(data)`
- 에러: `GlobalExceptionHandler`에서 자동 처리

### QueryDSL
- `CustomRepository` 인터페이스 + `Impl` 클래스 패턴
- `JPAQueryFactory` 주입 (`QuerydslConfig`에서 Bean 등록)
- 기본적으로 도메인 엔티티 반환, 성능이 중요한 읽기 전용 조회는 DTO Projection 허용

### Token
- Access Token: stateless JWT + AES-256-GCM 암호화
- Refresh Token: DB 저장 (refresh_tokens 테이블)
- 로그아웃 시 refresh token hard delete (soft delete 안 함)
- `TokenDomainService`: issueTokens, resolveUserId, revokeAllByUserId

### Auth Flow
- 회원가입/로그인: `UserDomainService` (authenticate/register) + `TokenDomainService` (issueTokens)
- Platform(SUPPORTERS/LMS) + Role(ADMIN/TEACHER/STUDENT) 기반 권한
- 클라이언트가 로그인 시 role 지정

## Build & Lint
```bash
./gradlew clean build        # 전체 빌드 + 테스트
./gradlew ktlintCheck        # 린트 검사
./gradlew ktlintFormat       # 자동 포맷팅
```
