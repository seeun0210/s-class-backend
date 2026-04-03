# S-Class Backend

학원 관리 플랫폼 백엔드 서버

## Tech Stack

- **Framework**: Spring Boot 4.0.3, Kotlin 2.1.10, Java 21
- **Database**: MySQL, JPA + QueryDSL
- **Auth**: JWT (AES-256-GCM) + BCrypt
- **Lint**: ktlint 1.5.0

## Engineering Blog

<!-- BLOG_POSTS_START -->
- [@Scheduled 대신 Quartz — 동적 스케줄과 취소](https://seeun0210.github.io/s-class-backend/quartz-dynamic-scheduler-2026-04-02.html) — `아키텍처` · `DevOps` · 2026-04-02
- [알림톡, 트랜잭션이 끝난 뒤에 보내야 한다](https://seeun0210.github.io/s-class-backend/adr-async-alimtalk-2026-04-02.html) — `아키텍처 결정` · 2026-04-02
- [App Runner에서 앱이 뜨질 않는다](https://seeun0210.github.io/s-class-backend/troubleshooting-2026-03-25.html) — `트러블슈팅` · `DevOps` · 2026-03-25
- [MSA로 시작했다가 Modular Monolith로 바꾼 이야기](https://seeun0210.github.io/s-class-backend/architecture-monorepo-2026-03-08.html) — `아키텍처` · `Kotlin · Spring` · 2026-03-08
<!-- BLOG_POSTS_END -->

## Module Structure

```
SClass-Common           # 공통 (어노테이션, DTO, 예외, JWT, 유틸)
SClass-Domain           # 도메인 (엔티티, 리포지토리, 어댑터, 도메인서비스)
SClass-Infrastructure   # 외부 연동 (S3, GCS, OAuth 등)
SClass-Api-Lms          # LMS API (수업/탐구/정산)
SClass-Api-Backoffice   # 슈퍼어드민 Backoffice API
SClass-Api-Supporters   # 서포터즈 서비스 전용 API
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

- `develop` — 개발 통합 (default branch)
- `main` — 프로덕션
- `feat/*`, `fix/*`, `refactor/*` — 작업 브랜치 → `develop`으로 PR

## 로컬에서 Dev DB 접속

[로컬 Dev DB 접속 가이드](DEV_DB_ACCESS.md) 참고

## AWS Architecture

![Architecture](architecture.svg)

## CI/CD & Git Flow

![CI/CD & Git Flow](cicd-git-flow.svg)
