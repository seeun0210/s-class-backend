---
name: pr
description: 현재 브랜치의 변경사항을 분석하여 PR 자동 생성
disable-model-invocation: true
allowed-tools: Bash, Read, Glob, Grep
---

현재 브랜치의 변경사항을 분석하여 develop 브랜치로 PR을 생성해줘.

## 절차

1. `git diff develop...HEAD`와 `git log develop..HEAD --oneline`으로 변경사항 분석
2. 변경된 파일들을 읽고 어떤 모듈이 영향받는지 파악
3. PR 제목 작성: 컨벤션 prefix 사용 (`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`, `test:`) + 한글 설명
4. `.github/pull_request_template.md` 템플릿 기반으로 PR 본문 작성:
   - Summary: 변경사항 요약
   - Changes: 주요 변경 bullet points
   - Affected Modules: 영향받는 모듈 체크
   - Test Plan: 검증 항목 체크
   - Checklist: 컨벤션 준수 확인
5. `gh pr create --base develop`로 PR 생성
6. 생성된 PR URL 출력

## 규칙

- PR 제목은 70자 이내
- 커밋이 없으면 PR 생성하지 말고 안내

## 추가 컨텍스트

$ARGUMENTS
