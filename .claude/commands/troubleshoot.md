현재 대화에서 진행한 트러블슈팅 내용을 HTML 페이지로 정리하고 GitHub Pages에 배포해줘.

## 절차

1. 현재 대화 컨텍스트에서 트러블슈팅 내용을 분석:
   - 증상 (에러 메시지, 로그)
   - 디버깅 과정 (시도한 것들, 실패/성공)
   - 근본 원인
   - 해결 방법
   - 교훈

2. `gh-pages` 브랜치로 전환:
   ```bash
   git stash  # 현재 변경사항 보존
   git checkout gh-pages
   ```

3. HTML 파일 생성: `troubleshooting-YYYY-MM-DD-<slug>.html`
   - 다크 테마 (GitHub 스타일: bg #0d1117)
   - 타임라인 형태로 디버깅 과정 표시 (ERROR → FIX → INSIGHT)
   - 코드 블록, 에러 박스, 성공 박스 활용
   - 교훈 테이블
   - 상단에 `← Back` 링크 (`index.html`)

4. `index.html`에 새 항목 추가:
   - 카드 형태로 제목, 설명, 날짜 표시
   - 최신 항목이 위로

5. 커밋 & 푸시:
   ```bash
   git add index.html troubleshooting-*.html
   git commit -m "docs: <트러블슈팅 제목>"
   git push origin gh-pages
   ```

6. 원래 브랜치로 복귀:
   ```bash
   git checkout <원래브랜치>
   git stash pop  # 변경사항 복원
   ```

7. GitHub Pages URL 출력

## 스타일 가이드

- 태그 색상: ERROR(#f85149), FIX(#3fb950), INSIGHT(#d2a8ff)
- 박스: error-box(빨간 테두리), success-box(초록 테두리), info-box(파란 테두리)
- 타임라인: 왼쪽 세로선 + 원형 마커
- 폰트: -apple-system, BlinkMacSystemFont, SF Mono (코드)
- 반응형: max-width 860px

## 규칙

- 민감 정보(시크릿, 비밀번호, API 키)는 절대 포함하지 않음
- 에러 메시지는 원문 그대로 포함 (검색 가능하도록)
- 한국어로 작성
- memory/troubleshooting.md에도 요약 기록 추가

## 추가 컨텍스트

$ARGUMENTS
