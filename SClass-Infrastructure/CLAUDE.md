# SClass-Infrastructure Module

## 역할
외부 서비스 연동 (클라우드 스토리지, OAuth 등). Common만 의존.

## 패키지 구조
```
com.sclass.infrastructure/
├── s3/     # AWS S3 클라이언트
└── gcs/    # Google Cloud Storage 클라이언트
```

## 규칙
- 외부 API/SDK 호출은 이 모듈에서만
- Domain 모듈에 의존하지 않음
- 인터페이스는 여기서 정의, Api 모듈에서 사용
- 설정은 @ConfigurationProperties 사용

## 향후 추가 예정
- `oauth/` — Google, Kakao OAuth 클라이언트

## 의존성
- Common
- spring-boot-starter
- AWS SDK S3
- Google Cloud Storage
- spring-web (OAuth HTTP 호출용)
