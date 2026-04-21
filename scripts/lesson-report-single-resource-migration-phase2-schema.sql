-- Phase 2. lessonReport 단일 리소스화를 위한 스키마 변경 단계.
-- MySQL의 ALTER TABLE은 implicit commit을 발생시키므로
-- Phase 1 데이터 정리 결과를 검증한 뒤 별도로 실행한다.

ALTER TABLE lesson_reports
    DROP INDEX uk_lesson_reports_lesson_version;

ALTER TABLE lesson_reports
    DROP COLUMN version;

ALTER TABLE lesson_reports
    ADD CONSTRAINT uk_lesson_reports_lesson UNIQUE (lesson_id);
