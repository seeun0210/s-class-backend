# SClass-Api-Lms Module

## 역할
LMS(수업/탐구/정산) API 서버. Platform = LMS.

## 패키지 구조
```
com.sclass.lms/
├── auth/
│   ├── controller/   # OAuthController
│   ├── dto/          # OAuthLoginRequest, OAuthLoginResponse, etc.
│   └── usecase/      # OAuthLoginUseCase
├── file/
│   ├── controller/   # FileController
│   ├── dto/          # FileResponse
│   └── usecase/      # ReadFileUseCase
└── config/           # SwaggerConfig
```

## 규칙
- Supporters 모듈과 동일한 패턴 (controller/dto/usecase)
- Platform은 항상 `Platform.LMS` 고정
- 공통 도메인 로직은 Domain 모듈의 DomainService 재사용
  - e.g., `UserDomainService.authenticate()` 호출 시 `Platform.LMS` 전달
- Controller, UseCase, DTO는 이 모듈에서 별도 정의

## 의존성
- Common, Domain, Infrastructure
- spring-boot-starter-web
- spring-boot-starter-validation
