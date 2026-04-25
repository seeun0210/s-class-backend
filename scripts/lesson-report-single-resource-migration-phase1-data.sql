-- Phase 1. lessonReport를 lesson당 단일 리소스로 전환하기 위한 데이터 정리 단계.
-- MySQL 기준 수동 실행 스크립트.
--
-- 변경 내용
-- 1. lesson별 최신 version 리포트만 유지
-- 2. 제거되는 리포트의 첨부 매핑 삭제
-- 3. 제거 대상 리포트 삭제

START TRANSACTION;

CREATE TEMPORARY TABLE keep_lesson_reports (
    id BIGINT PRIMARY KEY
);

INSERT INTO keep_lesson_reports (id)
SELECT lr.id
FROM lesson_reports lr
JOIN (
    SELECT lesson_id, MAX(version) AS max_version
    FROM lesson_reports
    GROUP BY lesson_id
) latest
    ON latest.lesson_id = lr.lesson_id
   AND latest.max_version = lr.version;

DELETE lrf
FROM lesson_report_files lrf
JOIN lesson_reports lr ON lr.id = lrf.lesson_report_id
LEFT JOIN keep_lesson_reports keep_lr ON keep_lr.id = lr.id
WHERE keep_lr.id IS NULL;

DELETE lr
FROM lesson_reports lr
LEFT JOIN keep_lesson_reports keep_lr ON keep_lr.id = lr.id
WHERE keep_lr.id IS NULL;

DROP TEMPORARY TABLE keep_lesson_reports;

COMMIT;
