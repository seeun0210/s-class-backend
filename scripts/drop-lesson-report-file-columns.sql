-- LessonReport 파일 첨부 구조 변경: 단일 파일 FK → lesson_report_files 조인 테이블
-- 적용 시점: lesson_report_files 테이블이 생성되고 기존 파일 데이터 이관 후 실행
--
-- 이관 스크립트 (기존 데이터 보존 시):
-- INSERT INTO lesson_report_files (lesson_report_id, file_id, created_at, updated_at)
-- SELECT id, report_file_id, NOW(), NOW() FROM lesson_reports WHERE report_file_id IS NOT NULL
-- UNION ALL
-- SELECT id, class_video_file_id, NOW(), NOW() FROM lesson_reports WHERE class_video_file_id IS NOT NULL;

ALTER TABLE lesson_reports DROP COLUMN report_file_id;
ALTER TABLE lesson_reports DROP COLUMN class_video_file_id;
