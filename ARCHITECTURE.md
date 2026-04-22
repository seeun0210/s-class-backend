# S-Class Backend Architecture

## Runtime Modules

| Module | Entry Point | 로컬 기본 포트 | 현재 자동 배포 |
| --- | --- | --- | --- |
| `SClass-Api-Supporters` | `SupportersApplication` | `8081` | 예 |
| `SClass-Api-Backoffice` | `BackofficeApplication` | `8082` | 예 |
| `SClass-Batch` | `BatchApplication` | 없음 | 아니오 |

현재 저장소에는 `SClass-Api-Lms` 모듈이 없습니다. 예전 문서나 다이어그램에 남아 있었다면 모두 구버전 기준입니다.

## Module Dependencies

```text
SClass-Api-Supporters  ┐
SClass-Api-Backoffice  ├─> SClass-Domain ──> SClass-Common
SClass-Batch           ┘
                       └─> SClass-Infrastructure ──> SClass-Common
```

- `SClass-Common`은 최하위 공통 모듈입니다.
- `SClass-Domain`은 도메인 엔티티, 리포지토리, 어댑터, 도메인 서비스를 가집니다.
- `SClass-Infrastructure`는 S3, OAuth, NicePay, 알림톡, Quartz 등 외부 연동을 가집니다.
- 부트 애플리케이션 모듈은 `Domain`, `Infrastructure`, `Common`을 조합합니다.

## Layer Rule

```text
Controller -> UseCase -> DomainService -> Adaptor -> Repository
```

| Layer | 위치 | 역할 |
| --- | --- | --- |
| `Controller` | API 모듈 | HTTP 요청/응답 |
| `UseCase` | API 모듈 | 애플리케이션 유즈케이스 조합 |
| `DomainService` | Domain 모듈 | 비즈니스 규칙, 상태 변경 |
| `Adaptor` | Domain 모듈 | Repository 래핑, 조회/저장 책임 |
| `Repository` | Domain 모듈 | Spring Data JPA 인터페이스 |

## Deployment Model

| Environment | 진입점 | 런타임 | 비고 |
| --- | --- | --- | --- |
| `dev` | Route 53 wildcard `*.dev.sclass.click` | EC2 + Nginx/Certbot + Docker Compose | GitHub Actions가 SSM으로 원격 배포 |
| `prod` | Route 53 A Alias -> ALB | ECS Fargate(private subnet) | 서비스별 독립 스케일링 |

공유 리소스는 다음과 같습니다.

- shared VPC(public/private subnet)
- NAT instance + S3 Gateway Endpoint
- 환경별 RDS MySQL
- prod 전용 ElastiCache Redis
- 환경별 S3 bucket + CloudFront static CDN
- ECR, SSM Parameter Store, CloudWatch

## Local Development Defaults

| 항목 | 기본값 |
| --- | --- |
| Supporters API | `http://localhost:8081` |
| Backoffice API | `http://localhost:8082` |
| Dev DB tunnel | `127.0.0.1:13306/sclass_dev` |
| Redis | `127.0.0.1:6379` |
| MinIO | `http://127.0.0.1:9000` |

관련 문서:

- [README.md](README.md)
- [DEV_DB_ACCESS.md](DEV_DB_ACCESS.md)

## CI/CD Notes

- `cd.yml`의 자동 배포 매트릭스는 `supporters-api`, `backoffice-api` 두 서비스만 포함합니다.
- `SClass-Batch`는 현재 CI에서 빌드/테스트되지만 자동 배포되지는 않습니다.
- `develop` push는 dev EC2, `main` push는 prod ECS로 배포됩니다.
