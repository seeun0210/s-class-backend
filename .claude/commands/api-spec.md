현재 브랜치의 변경사항을 분석하여 FE 개발자에게 전달할 API 변경 스펙을 정리해줘.

## 절차

1. `git diff origin/develop...HEAD --name-only`로 변경된 파일 목록 파악
2. 변경된 파일 중 API 관련 파일 필터링:
   - `**/controller/**` — 엔드포인트 변경 감지
   - `**/dto/**` (Api 모듈 내) — Request/Response DTO 변경 감지
   - `**/domain/**/domain/**` — 엔티티/Enum 변경 감지 (응답에 영향)
   - `**/exception/**` — 에러코드 변경 감지
3. 변경된 Controller 파일을 읽고 각 엔드포인트 정보 추출:
   - HTTP 메서드 + URL 패턴 (`@GetMapping`, `@PostMapping` 등)
   - 인증 필요 여부 (`@AuthenticatedUser` 등)
   - Path Variable, Query Parameter, Request Body 타입
4. 변경된 Request/Response DTO 파일을 읽고 필드 상세 추출:
   - 필드명, 타입, nullable 여부, validation 어노테이션
   - 중첩 DTO가 있으면 함께 포함
5. 변경된 Enum/VO가 DTO에서 사용되는 경우 해당 값 목록 포함
6. 새로 추가된 ErrorCode가 있으면 포함
7. 엔드포인트별 cURL 예시 생성:
   - Base URL: `{{BASE_URL}}`
   - 인증 헤더: `Authorization: Bearer {{TOKEN}}`
   - Request Body는 예시값으로 채우기 (String → "example", Long → 1, Boolean → true 등)
8. 아래 출력 형식에 맞춰 마크다운으로 정리

## 변경 유형 분류

각 엔드포인트를 아래 기준으로 분류:
- **NEW**: develop에 없던 새 엔드포인트
- **MODIFIED**: 기존 엔드포인트의 Request/Response가 변경됨
- **DELETED**: 삭제된 엔드포인트

`git diff origin/develop...HEAD`의 실제 diff 내용을 보고 판단할 것.

## 출력 형식

```
# API 변경 스펙

> 브랜치: `{branch_name}`
> 생성일: {date}
> 영향 모듈: {modules}

---

## 📋 변경 요약

| 변경 | 메서드 | 엔드포인트 | 설명 |
|------|--------|-----------|------|
| 🆕 NEW | POST | /api/v1/xxx | 설명 |
| ✏️ MODIFIED | GET | /api/v1/yyy | 변경 내용 |
| 🗑️ DELETED | DELETE | /api/v1/zzz | 삭제 사유 |

---

## 🔵 [NEW] POST /api/v1/xxx — 설명

### Request

**Headers**
| 헤더 | 값 | 필수 |
|-----|---|------|
| Authorization | Bearer {{TOKEN}} | ✅ |
| Content-Type | application/json | ✅ |

**Path Parameters** (있을 경우)
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| id | String | 리소스 ID |

**Query Parameters** (있을 경우)
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|-------|------|
| page | Int | ❌ | 0 | 페이지 번호 |

**Request Body**
```json
{
  "field1": "example",
  "field2": 1,
  "field3": true
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| field1 | String | ✅ | 설명 |
| field2 | Long | ❌ | 설명 |

### Response

**성공 (200)**
```json
{
  "success": true,
  "data": {
    "id": "01HXXXXXXXXXXXXXXXXX",
    "field1": "example"
  }
}
```

| 필드 | 타입 | 설명 |
|-----|------|------|
| id | String (ULID) | 리소스 ID |
| field1 | String | 설명 |

### Error Codes (해당 API에서 발생 가능한 에러)
| 코드 | HTTP | 메시지 |
|-----|------|-------|
| XXX_001 | 404 | 리소스를 찾을 수 없습니다 |

### cURL

```bash
curl -X POST '{{BASE_URL}}/api/v1/xxx' \
  -H 'Authorization: Bearer {{TOKEN}}' \
  -H 'Content-Type: application/json' \
  -d '{
    "field1": "example",
    "field2": 1
  }'
```

---

(다음 엔드포인트 반복...)

## 📎 관련 Enum/타입 변경

### EnumName
| 값 | 설명 |
|---|------|
| VALUE_1 | 설명 |
| VALUE_2 | 설명 |

---

## ✏️ DTO 변경 사항

### SomeRequestDto
- `newField` (`String`): 필드 추가됨. (설명)
- `oldField` (`Int`): `String`에서 타입 변경됨.

### SomeResponseDto
- `removedField`: 필드 삭제됨.

```

## 규칙

- **출력은 반드시 위 마크다운 형식을 따를 것** — FE가 복사해서 바로 사용할 수 있어야 함
- 변경이 없는 API는 포함하지 않음
- Controller 변경이 없으면 (DTO만 변경 등) "DTO 변경 사항" 섹션으로 별도 정리
- Enum 값이 DTO 필드 타입으로 사용되면 가능한 값 목록을 해당 필드 설명에 인라인으로 포함
- cURL 예시의 값은 의미 있는 예시값 사용 (email → "user@example.com", name → "홍길동" 등)
- `ApiResponse<T>` 래핑을 반영하여 Response 예시에 `success`, `data` 구조 포함
- 페이징 응답인 경우 `Page<T>` 구조도 반영
- 변경 사항이 없으면 "현재 브랜치에 API 변경 사항이 없습니다." 출력

## 추가 컨텍스트

$ARGUMENTS
