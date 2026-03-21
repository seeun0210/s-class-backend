---
name: issue
description: GitHub 이슈 생성
argument-hint: "[이슈 내용]"
disable-model-invocation: true
allowed-tools: Bash, Read, Glob, Grep, AskUserQuestion
---

사용자의 요청을 분석하여 GitHub 이슈를 생성해줘.

## 절차

1. `$ARGUMENTS`를 분석하여 이슈 유형 판단 (버그 / 기능 요청)
2. 정보가 부족하면 `AskUserQuestion`으로 추가 정보 수집:
   - 이슈 유형 (버그 / 기능 요청)
   - 관련 모듈
   - 상세 내용
3. 이슈 제목 작성: 간결한 한글 제목
4. `.github/ISSUE_TEMPLATE/`의 양식에 맞춰 이슈 본문 작성
5. 적절한 라벨 지정:
   - 버그: `🐛 bug`
   - 기능: `✨ feature`
   - 모듈: `📦 module: common`, `🏛️ module: domain`, `🔌 module: infra`, `🎓 module: supporters`, `🛠️ module: management`, `⏰ module: batch`
6. `gh issue create --title "..." --body "..." --label "..."` 로 이슈 생성
7. 생성된 이슈 URL 출력

## 규칙

- 이슈 제목은 간결하게 (50자 이내)
- 본문은 한글로 작성
- 정보가 불충분하면 반드시 사용자에게 질문

## 추가 컨텍스트

$ARGUMENTS
