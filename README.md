# S-Class Backend

수행평가 매칭과 학원 관리를 하나의 도메인으로 연결하는 B2B2C 교육 플랫폼 백엔드 서버

- **Supporters** — 학생이 수행평가를 강사에게 의뢰하고 도움받는 매칭 서비스
- **LMS** — 학원 단위 수업·탐구·정산 관리 (원장, 강사, 학생)
- **Backoffice** — 플랫폼 운영 관리

## Tech Stack

- **Framework**: Spring Boot 4.0.3, Kotlin 2.1.10, Java 21
- **Database**: MySQL, JPA + QueryDSL
- **Auth**: JWT (AES-256-GCM) + BCrypt
- **Lint**: ktlint 1.5.0

## Engineering Blog

<!-- BLOG_POSTS_START -->
- [App Runner에서 외부 API 호출이 10초 만에 죽는다](https://seeun0210.github.io/s-class-backend/troubleshooting-nat-forward-2026-04-05.html) — `트러블슈팅` · `DevOps` · 2026-04-05
- [@Scheduled 대신 Quartz — 동적 스케줄과 취소](https://seeun0210.github.io/s-class-backend/quartz-dynamic-scheduler-2026-04-02.html) — `아키텍처` · `DevOps` · 2026-04-02
- [알림톡, 트랜잭션이 끝난 뒤에 보내야 한다](https://seeun0210.github.io/s-class-backend/adr-async-alimtalk-2026-04-02.html) — `아키텍처 결정` · 2026-04-02
- [App Runner에서 앱이 뜨질 않는다](https://seeun0210.github.io/s-class-backend/troubleshooting-2026-03-25.html) — `트러블슈팅` · `DevOps` · 2026-03-25
- [MSA로 시작했다가 Modular Monolith로 바꾼 이야기](https://seeun0210.github.io/s-class-backend/architecture-monorepo-2026-03-08.html) — `아키텍처` · `Kotlin · Spring` · 2026-03-08
<!-- BLOG_POSTS_END -->

## Module Structure

서비스별 트래픽 프로파일이 다르기 때문에(Supporters > LMS > Backoffice) API 모듈을 분리하여 독립 배포·스케일링이 가능하도록 설계했습니다. 도메인 모듈을 공유하여 강사·학생 등 핵심 엔티티의 중복 없이 여러 서비스에서 재사용합니다.

```
SClass-Common           # 공통 (어노테이션, DTO, 예외, JWT, 유틸)
SClass-Domain           # 도메인 (엔티티, 리포지토리, 어댑터, 도메인서비스)
SClass-Infrastructure   # 외부 연동 (S3, OAuth, NicePay, 알림톡, Quartz 등)
SClass-Api-Supporters   # Supporters API — 트래픽 최다, 독립 스케일링
SClass-Api-Lms          # LMS API — 학원 단위 수업·탐구·정산
SClass-Api-Backoffice   # Backoffice API — 내부 운영진 전용
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
