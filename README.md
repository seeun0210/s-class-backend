# S-Class Backend

학원 관리 플랫폼 백엔드 서버

## Tech Stack

- **Framework**: Spring Boot 4.0.3, Kotlin 2.1.10, Java 21
- **Database**: MySQL, JPA + QueryDSL
- **Auth**: JWT (AES-256-GCM) + BCrypt
- **Lint**: ktlint 1.5.0

## Module Structure

```
SClass-Common           # 공통 (어노테이션, DTO, 예외, JWT, 유틸)
SClass-Domain           # 도메인 (엔티티, 리포지토리, 어댑터, 도메인서비스)
SClass-Infrastructure   # 외부 연동 (S3, GCS, OAuth 등)
SClass-Api-Supporters   # 학생/학부모용 API
SClass-Api-Management   # 관리자/선생님용 API
SClass-Batch            # 배치 처리
```

## Getting Started

```bash
# 빌드
./gradlew clean build

# 린트 검사
./gradlew ktlintCheck

# 자동 포맷팅
./gradlew ktlintFormat
```

첫 빌드 시 Git hooks가 자동 설정됩니다 (커밋 전 lint + build 검증).

## Branch Strategy

- `main` — 프로덕션
- `develop` — 개발 통합
- `feat/*`, `fix/*`, `refactor/*` — 작업 브랜치 → `develop`으로 PR
