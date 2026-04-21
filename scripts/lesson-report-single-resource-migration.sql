-- lessonReport를 lesson당 단일 리소스로 전환한다.
-- MySQL 기준 수동 마이그레이션 스크립트.
--
-- 변경 내용
-- 1. lesson별 최신 version 리포트만 유지
-- 2. 제거되는 리포트의 첨부 매핑 삭제
-- 3. lesson_reports.version 컬럼 제거
-- 4. lesson_id 단일 unique 제약 추가

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

ALTER TABLE lesson_reports
    DROP INDEX uk_lesson_reports_lesson_version;

ALTER TABLE lesson_reports
    DROP COLUMN version;

ALTER TABLE lesson_reports
    ADD CONSTRAINT uk_lesson_reports_lesson UNIQUE (lesson_id);

DROP TEMPORARY TABLE keep_lesson_reports;

COMMIT;
