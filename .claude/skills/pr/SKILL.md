---
name: pr
description: 현재 브랜치의 변경사항을 분석하여 PR 생성 또는 업데이트
argument-hint: "[추가 컨텍스트]"
disable-model-invocation: true
allowed-tools: Bash, Read, Glob, Grep
---

현재 브랜치의 변경사항을 분석하여 develop 브랜치로 PR을 생성하거나, 이미 PR이 있으면 description을 업데이트해줘.

## 절차

1. `git fetch origin`으로 리모트 develop 브랜치의 최신 정보를 가져오기
2. `gh pr view --json number,title,url` 로 현재 브랜치에 열린 PR이 있는지 확인
3. `git diff origin/develop...HEAD`와 `git log origin/develop..HEAD --oneline`으로 변경사항 분석
4. 변경된 파일들을 읽고 어떤 모듈이 영향받는지 파악
5. PR 제목 작성: 컨벤션 prefix 사용 (`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`, `test:`) + 한글 설명
6. `.github/pull_request_template.md` 템플릿 기반으로 PR 본문 작성:
   - Summary: 변경사항 요약
   - Changes: 주요 변경 bullet points
   - Affected Modules: 영향받는 모듈 체크
   - Test Plan: 검증 항목 체크
   - Checklist: 컨벤션 준수 확인
7. PR 존재 여부에 따라:
   - **PR 없음**: `gh pr create --base develop`로 새 PR 생성
   - **PR 있음**: `gh pr edit --title "..." --body "..."`로 제목/본문 업데이트
8. PR URL 출력

## 규칙

- PR 제목은 70자 이내
- 커밋이 없으면 PR 생성/업데이트하지 말고 안내

## 추가 컨텍스트

$ARGUMENTS
