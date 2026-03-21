---
name: start-work
description: 작업 내용을 PR 단위로 분해하여 이슈 등록 및 worktree 생성
argument-hint: "[작업 내용]"
disable-model-invocation: true
allowed-tools: Bash, Read, Glob, Grep, AskUserQuestion
---

사용자의 작업 요청을 PR 단위로 분해하고, GitHub 이슈를 생성한 뒤, 첫 번째 작업을 위한 worktree를 세팅해줘.

## 절차

### 1. 작업 분석 및 분해

`$ARGUMENTS`를 분석하여 PR 단위의 세부 태스크로 분해:
- 코드베이스를 탐색하여 관련 모듈, 기존 코드 구조 파악
- 각 태스크가 하나의 PR로 완결될 수 있는 단위인지 확인
- 태스크 간 의존 순서 결정

### 2. 사용자 확인

`AskUserQuestion`으로 분해 결과를 확인받기:
- 각 태스크의 유형 (feat / fix / refactor / docs / chore / test)
- 각 태스크의 제목 (한글, 50자 이내)
- 관련 모듈
- 태스크 순서
- 사용자가 수정을 원하면 반영 후 진행

### 3. 상위 이슈 생성

에픽 성격의 상위 이슈를 생성:
```bash
gh issue create \
  --title "상위 이슈 제목" \
  --body "본문" \
  --label "✨ feature"
```
- 본문에 하위 태스크 체크리스트 포함 (태스크 제목 나열)
- 라벨: 전체 작업의 대표 타입 라벨

### 4. 하위 이슈 생성

각 PR 단위 태스크를 개별 이슈로 생성:
```bash
gh issue create \
  --title "하위 이슈 제목" \
  --body "본문" \
  --label "타입라벨" --label "모듈라벨"
```
- 본문 구성 (`.github/ISSUE_TEMPLATE/feature_request.yml` 양식 기반):
  - **기능 설명**: 태스크 상세 내용
  - **배경 / 동기**: 상위 이슈 참조 (`Parent: #상위이슈번호`)
  - **제안하는 구현 방법**: 구현 방향 (있으면)
- 타입 라벨: `✨ feature`, `🐛 bug`, `♻️ refactor`, `📝 docs`, `🔧 chore`, `🧪 test`
- 모듈 라벨: `📦 module: common`, `🏛️ module: domain`, `🔌 module: infra`, `🎓 module: supporters`, `🛠️ module: management`, `⏰ module: batch`

### 5. 첫 번째 이슈 worktree 생성

첫 번째 서브 이슈 기반으로 작업 환경 생성:
```bash
git fetch origin
git worktree add .claude/worktrees/<브랜치명> -b <브랜치명> origin/develop
```
- 브랜치명 규칙: `{prefix}/{issue-number}-{short-description}`
  - prefix는 이슈 유형에 따라 결정: `feat/`, `fix/`, `refactor/`, `docs/`, `chore/`, `test/`
  - short-description은 영문 kebab-case
  - 예: `feat/42-user-profile-api`

### 6. 결과 출력

생성된 모든 이슈 목록과 worktree 정보 출력:
- 상위 이슈 URL
- 하위 이슈 URL 목록 (순서대로)
- worktree 경로 및 브랜치명
- 다음 작업 안내

## 규칙

- 이슈 제목은 간결하게 (50자 이내), 한글로 작성
- 브랜치 prefix는 `pr-labels.yml` 패턴과 일치시킴 (`feat/` → `✨ feature` 등)
- 정보가 부족하면 반드시 `AskUserQuestion`으로 추가 정보 수집
- worktree base는 `origin/develop`
- worktree 경로는 `.claude/worktrees/` 하위

## 추가 컨텍스트

$ARGUMENTS
