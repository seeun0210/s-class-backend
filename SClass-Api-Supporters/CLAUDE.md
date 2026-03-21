# SClass-Api-Supporters Module

## 역할
선생님/학생/학부모용 API 서버. Platform = SUPPORTERS.

## 패키지 구조
```
com.sclass.supporters/
├── auth/
│   ├── controller/   # AuthController
│   ├── dto/          # RegisterRequest, LoginRequest, RefreshRequest, TokenResponse
│   └── usecase/      # RegisterUseCase, LoginUseCase, RefreshUseCase, LogoutUseCase
└── file/
    ├── controller/   # FileController
    ├── dto/          # PresignedUrlRequest, PresignedUrlResponse
    └── usecase/      # CreateFileUseCase
```

## 새 기능 추가 시 패턴

### 1. DTO 생성 (dto/)
```kotlin
data class XxxRequest(
    @field:NotBlank val name: String,
)

data class XxxResponse(
    val id: String,
    val name: String,
)
```

### 2. UseCase 생성 (usecase/)
```kotlin
@UseCase
class CreateXxxUseCase(
    private val xxxDomainService: XxxDomainService, // 또는 Adaptor 직접
) {
    @Transactional
    fun execute(request: XxxRequest): XxxResponse {
        // DomainService/Adaptor 호출
        // Response 변환 후 반환
    }
}
```

### 3. Controller 생성 (controller/)
```kotlin
@RestController
@RequestMapping("/api/v1/xxx")
class XxxController(
    private val createXxxUseCase: CreateXxxUseCase,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: XxxRequest,
    ): ApiResponse<XxxResponse> = ApiResponse.success(createXxxUseCase.execute(request))
}
```

## 규칙
- Platform은 항상 `Platform.SUPPORTERS` 고정
- Controller는 얇게 — UseCase 호출 + ApiResponse 래핑만
- UseCase에서 `@Transactional` 선언
- Request DTO에 Validation 어노테이션 사용 (@NotBlank, @Email 등)
- 도메인 엔티티를 Response로 직접 노출 금지 — DTO로 변환

## 의존성
- Common, Domain, Infrastructure
- spring-boot-starter-web
- spring-boot-starter-validation
- mockk (테스트)
