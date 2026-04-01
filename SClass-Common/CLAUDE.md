# SClass-Common Module

## 역할
모든 모듈에서 공유하는 공통 코드. 최하위 의존성 모듈.

## 패키지 구조
```
com.sclass.common/
├── annotation/     # @UseCase, @DomainService, @Adaptor
├── dto/            # ApiResponse, ErrorResponse
├── exception/      # ErrorCode(interface), BusinessException, GlobalErrorCode, GlobalExceptionHandler
├── jwt/            # JWT 토큰 생성/파싱, AES 암호화
│   └── exception/  # TokenErrorCode, TokenExpiredException, InvalidTokenException, RefreshTokenExpiredException
└── util/           # Logger 등 유틸리티
```

## 주요 클래스

### 커스텀 어노테이션
- `@UseCase` — Api 모듈의 유즈케이스 클래스에 사용
- `@DomainService` — Domain 모듈의 비즈니스 로직 클래스에 사용
- `@Adaptor` — Domain 모듈의 데이터 접근 래퍼에 사용
- `@EventHandler` - Api모듈 트랜잭션 이벤트 리스너. AFTER_COMMIT 비동기 알림 처리
- 모두 내부적으로 `@Component` 포함

### JWT
- `JwtProperties` — auth.jwt.* 설정 바인딩 (@ConfigurationProperties)
- `TokenEncryptionProperties` — auth.token-encryption.* 설정 바인딩
- `JwtTokenProvider` — JWT 생성/파싱 (JJWT 0.12.6, HS256)
  - `generateAccessToken(userId, role)` → JWT 문자열
  - `generateRefreshToken(userId)` → JWT 문자열
  - `parseAccessToken(token)` → AccessTokenInfo
  - `parseRefreshToken(token)` → userId
- `AesTokenEncryptor` — AES-256-GCM 암호화/복호화
  - `encrypt(plainToken)` → 암호화된 Base64 URL-safe 문자열
  - `decrypt(encryptedToken)` → 원본 JWT 문자열
- `AccessTokenInfo` — userId + roles 담는 data class
- `JwtConfig` — @EnableConfigurationProperties 등록

### API 응답
- `ApiResponse<T>` — 통일된 응답 포맷 (`success`, `data`, `error`)
- `ErrorResponse` — 에러 응답 (`code`, `message`, `status`)
- `GlobalExceptionHandler` — BusinessException, Validation, 미처리 예외 핸들링

### 예외
- `ErrorCode` — interface (code, message, httpStatus)
- `BusinessException` — 모든 도메인 예외의 부모
- `GlobalErrorCode` — 공통 에러 (INVALID_INPUT, UNAUTHORIZED, FORBIDDEN 등)

## 규칙
- 다른 모듈에 의존하지 않음
- 도메인 로직 포함 금지
- 새 어노테이션 추가 시 여기에 정의
- JWT 관련 코드는 모두 jwt/ 패키지에
