# SClass-Domain Module

## 역할
도메인 엔티티, 비즈니스 로직, 데이터 접근 계층. Common만 의존.

## 패키지 구조
```
com.sclass.domain/
├── common/
│   ├── model/    # BaseTimeEntity
│   └── vo/       # Ulid
├── config/       # QuerydslConfig
└── domains/
    └── {feature}/
        ├── domain/      # Entity, Enum, VO
        ├── repository/  # JpaRepository + CustomRepository
        ├── adaptor/     # @Adaptor
        ├── service/     # @DomainService (필요 시만)
        ├── exception/   # ErrorCode + Exception
        ├── dto/         # 서비스 반환용 DTO (필요 시)
        └── config/      # 도메인별 설정 (필요 시)
```

## 현재 도메인
- `user` — User, UserRole, AuthProvider, Platform, Role, Grade
- `token` — RefreshToken, TokenType
- `file` — File, FileType

## 계층별 규칙

### Entity
- `@Id` ULID (VARCHAR 26), `BaseTimeEntity` 상속
- 컬럼 간 빈 줄 허용
- `companion object { fun create(...) }` 팩토리 (필요 시)
- 도메인 로직은 엔티티 메서드로 (e.g., `User.changePassword()`)

### Repository
- `JpaRepository<Entity, String>` 상속
- Spring Data 메서드 네이밍 규칙 사용
- 복잡한 쿼리: `CustomRepository` 인터페이스 + `Impl` 클래스 (QueryDSL)
- CustomRepository 구현 시 `JPAQueryFactory` 주입

### Adaptor (@Adaptor)
- Repository를 래핑하여 읽기 + 쓰기 담당
- 조회 실패 시 도메인 예외 throw
- `findByXxx` → 없으면 예외, `findByXxxOrNull` → nullable
- Repository 직접 노출 금지 (외부에서는 Adaptor만 사용)

### DomainService (@DomainService)
- 비즈니스 로직이 있을 때만 생성
- Adaptor를 주입받아 사용 (Repository 직접 참조 금지)
- 같은 도메인 내 다른 Service 참조 가능 (e.g., UserDomainService → PasswordService)
- 도메인 엔티티를 반환 (DTO/VO 아님)
- `@Transactional` 선언 가능 (단독 호출될 수 있는 경우). 주 트랜잭션 경계는 UseCase

### Exception
- `{Feature}ErrorCode` enum: ErrorCode 인터페이스 구현
- `{Feature}Exception` class: BusinessException 상속 + companion EXCEPTION 싱글턴
- 코드 네이밍: `{FEATURE}_{NUMBER}` (e.g., USER_001, AUTH_001)

## 주요 서비스
- `UserDomainService` — register(회원가입), authenticate(인증)
- `PasswordService` — BCrypt hash/matches
- `TokenDomainService` — issueTokens, resolveUserId, revokeAllByUserId

## 의존성
- Common (api)
- spring-boot-starter-data-jpa
- QueryDSL 5.1.0
- ULID Creator 5.2.3
- spring-security-crypto (BCrypt)
