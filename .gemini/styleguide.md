# Gemini Code Assist Review Style Guide

## Language

- 모든 PR 요약, 리뷰 코멘트, inline comment, 질문 응답은 한국어로 작성한다.
- 코드 식별자, 클래스명, 메서드명, API 경로, enum 값, 에러 코드, 로그 메시지는 원문을 유지한다.
- 영어 기술 용어가 더 자연스러운 경우에도 한국어 설명을 우선하고, 필요한 경우에만 괄호로 병기한다.
- 리뷰 코멘트는 간결하게 작성하되, 문제의 근거와 수정 방향을 함께 제시한다.

## Review Preparation

- 리뷰 전에 루트 `CLAUDE.md`, `ARCHITECTURE.md`, 변경 파일과 같은 모듈의 `CLAUDE.md`가 있으면 먼저 확인한다.
- 변경 파일과 같은 feature/package, 같은 layer(controller/usecase/domain/adaptor/repository/test)의 기존 구현을 우선 비교 대상으로 삼는다.
- 기존 코드에서 반복적으로 쓰이는 구조, 네이밍, 예외 처리, 트랜잭션, DTO 변환, 테스트 스타일을 프로젝트 컨벤션으로 간주한다.
- 일반적인 베스트 프랙티스와 프로젝트 컨벤션이 충돌하면, 명백한 버그, 보안 문제, 데이터 정합성 문제가 아닌 한 프로젝트 컨벤션을 우선한다.
- 새 코드가 기존 패턴과 다를 때만 스타일성 리뷰를 남기고, 가능하면 어떤 기존 패턴과 다른지 함께 설명한다.

## Architecture Rules

- 모듈 의존 방향은 `Api-Supporters`/`Api-Backoffice`/`Batch` -> `Domain` + `Infrastructure` + `Common` -> `Common`을 유지한다.
- `Domain` 모듈은 `Common`만 의존해야 하며, API 모듈이나 Infrastructure 모듈에 의존하면 안 된다.
- `Infrastructure` 모듈은 외부 API/SDK 연동을 담당하고 `Domain`에 의존하면 안 된다.
- API 계층 흐름은 `Controller -> UseCase -> DomainService -> Adaptor -> Repository`를 따른다.
- Controller는 HTTP 요청/응답, validation annotation, `ApiResponse.success(...)` 래핑에 집중하고 비즈니스 로직을 넣지 않는다.
- Controller는 UseCase만 주입받고, DomainService/Adaptor/Repository를 직접 주입받지 않는다.
- API 모듈에서는 기본적으로 `1 API = 1 UseCase` 원칙을 따른다.

## UseCase Rules

- UseCase는 `@UseCase`를 사용하고 API 모듈의 `usecase` 패키지에 둔다.
- UseCase의 public entrypoint는 기존 패턴처럼 `execute(...)`를 우선 사용한다.
- UseCase는 주 트랜잭션 경계이므로 조회 전용은 `@Transactional(readOnly = true)`, 상태 변경은 `@Transactional`을 선언한다.
- UseCase는 Request DTO를 받아 도메인 호출을 조합하고 Response DTO를 반환한다.
- 도메인 엔티티를 API 응답으로 직접 노출하지 않고 Response DTO로 변환한다.
- 단순 조회나 저장은 Adaptor를 직접 사용하고, 검증/상태 변경 같은 비즈니스 규칙은 DomainService 또는 엔티티 메서드에 둔다.

## Domain Rules

- Entity는 도메인 상태와 상태 변경 메서드를 가진다. 상태 전이 검증은 가능한 한 엔티티 또는 DomainService에 둔다.
- Entity ID는 기존 도메인별 ID 전략을 따른다. 문자열 ID는 ULID 26자 패턴을, numeric entity는 기존 `Long` ID 패턴을 따른다.
- Entity는 `BaseTimeEntity` 상속, JPA annotation, enum string 저장 등 기존 주변 도메인의 매핑 방식을 따른다.
- Adaptor는 Repository를 감싸며 조회, 저장, 삭제를 담당한다.
- 필수 조회는 `findByXxx(...)`에서 도메인별 `BusinessException`을 던지고, nullable 조회는 `findByXxxOrNull(...)` 패턴을 사용한다.
- DomainService는 검증, 상태 변경, 여러 adaptor 조합이 필요한 비즈니스 규칙에만 사용한다. 단순 CRUD나 조회 전용 로직만으로 새 DomainService를 만들지 않는다.
- DomainService는 Repository를 직접 주입하지 않고 Adaptor를 사용한다.

## Exception And Response Rules

- 도메인 예외는 `ErrorCode` enum과 `BusinessException` 하위 exception class 조합을 따른다.
- 예외 class는 singleton/object가 아니라 매번 새 instance를 만들 수 있는 class로 둔다.
- HTTP 에러 응답은 `GlobalExceptionHandler`의 `ApiResponse.error(...)` 흐름을 따른다.
- API 성공 응답은 `ApiResponse<T>`로 래핑한다. body가 없어도 기존 컨벤션에 맞춰 `ApiResponse.success()` 또는 `ApiResponse.success(Unit)`을 사용한다.
- validation은 Request DTO 또는 Controller parameter의 jakarta validation annotation으로 처리한다.

## Repository And Query Rules

- Spring Data repository만으로 부족한 조회는 `CustomRepository` interface와 `CustomRepositoryImpl` 구현 패턴을 사용한다.
- QueryDSL은 `JPAQueryFactory`를 주입받아 사용한다.
- 기본 조회 결과는 도메인 엔티티를 우선 반환하고, 목록/통계/성능상 필요한 읽기 전용 조회는 DTO projection을 허용한다.
- Pageable/Sort, count query, fetch join, group by는 기존 repository 구현과 같은 방식으로 맞춘다.

## Security And Consistency Checks

- 인증 사용자 ID는 기존 annotation인 `@CurrentUserId`, 역할은 `@CurrentUserRole`, 조직은 `@OrganizationId` 패턴을 우선 사용한다.
- Supporters API에서는 필요한 경우 `Platform.SUPPORTERS` 고정 규칙을 확인한다.
- Backoffice API에서는 관리자/조직/역할 경계가 누락되지 않았는지 확인한다.
- 결제, 수강 신청, 코인, 파일 삭제, 외부 webhook, OAuth, 알림톡, 배치 작업은 멱등성, 중복 처리, 권한, 상태 전이, 트랜잭션 경계를 우선 리뷰한다.
- 파일/S3 같은 외부 리소스 삭제는 다른 도메인 참조 가능성을 고려해 즉시 삭제가 안전한지 확인한다.
- 비동기 이벤트나 `@TransactionalEventListener`는 트랜잭션 commit 이후 실행 의도가 유지되는지 확인한다.

## Test Rules

- 새 UseCase에는 기존 `*UseCaseTest` 스타일의 단위 테스트를 우선 추가한다.
- Controller 변경에는 기존 `*ControllerIntegrationTest` 패턴과 MockMvc 기반 테스트를 따른다.
- Domain 규칙 변경에는 domain/entity/service/adaptor test를 추가한다.
- 테스트는 JUnit 5와 mockk의 기존 사용 방식을 따른다.
- 테스트명은 기존처럼 한국어 backtick display name을 허용한다.
- 버그 수정 리뷰에서는 실패 케이스, 권한 실패, 상태 전이 실패, idempotent 재호출, 중복 요청, 경계값 테스트 누락을 우선 확인한다.

## Comment Quality

- correctness, authorization, validation, transaction boundary, data consistency, state transition, external integration failure, test coverage를 우선 리뷰한다.
- ktlint가 자동으로 잡을 포맷팅이나 사소한 취향 차이는 리뷰하지 않는다.
- 대규모 리팩터링 제안은 현재 변경의 안정성과 직접 관련이 있을 때만 남긴다.
- 기존에 널리 쓰이는 패턴을 바꾸라고 제안하려면, 그 패턴이 실제 버그나 운영 리스크로 이어지는 근거를 함께 제시한다.
- 확신이 낮은 내용은 단정하지 말고 확인 질문 또는 "확인 필요"로 표현한다.
