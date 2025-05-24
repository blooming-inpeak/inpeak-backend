-- 02-data.sql
-- 개발 환경용 초기 데이터 스크립트

-- 인코딩 설정
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 오류 처리를 위한 구성
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE;
SET SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- 디버깅을 위한 로그 출력
SELECT '개발 환경용 데이터 초기화 스크립트를 시작합니다.' AS message;

-- 트랜잭션 시작
START TRANSACTION;

-- -------------------------------
-- 1. 회원 데이터
-- -------------------------------
INSERT INTO members
(id, correct_answer_count, created_at, total_question_count, updated_at,
 kakao_id, nickname, provider, registration_status, kakao_email)
VALUES
    (1, 0, NOW(), 0, NOW(), 1, "test1", "KAKAO", "COMPLETED", "test1@test.com"),
    (2, 0, NOW(), 0, NOW(), 2, "test2", "KAKAO", "INITIATED", "test2@test.com"),
    (3, 0, NOW(), 0, NOW(), 3, "test3", "KAKAO", "COMPLETED", "test3@test.com"),
    (4, 0, NOW(), 0, NOW(), 4, "test4", "KAKAO", "INITIATED", "test4@test.com"),
    (5, 0, NOW(), 0, NOW(), 5, "test5", "KAKAO", "COMPLETED", "test5@test.com"),
    (6, 0, NOW(), 0, NOW(), 6, "test6", "KAKAO", "INITIATED", "test6@test.com");

SELECT CONCAT('회원 데이터: ', COUNT(*), '개 행') AS message FROM members;

-- -------------------------------
-- 2. 관심사 데이터
-- -------------------------------
INSERT INTO member_interests (member_id, interest_type)
VALUES
    (1, "DATABASE"),
    (1, "REACT"),
    (1, "SPRING"),
    (3, "DATABASE"),
    (3, "REACT"),
    (3, "SPRING"),
    (5, "DATABASE"),
    (5, "REACT"),
    (5, "SPRING");

SELECT CONCAT('관심사 데이터: ', COUNT(*), '개 행') AS message FROM member_interests;

-- -------------------------------
-- 3. 질문 데이터
-- -------------------------------
INSERT INTO questions
(id, created_at, updated_at, best_answer, content, type)
VALUES
    (1, NOW(), NOW(), "SQL에서 인덱스는 데이터베이스 테이블의 검색 속도를 향상시키는 데이터 구조입니다.", "데이터베이스에서 인덱스가 무엇인가요?", "REACT"),
    (2, NOW(), NOW(), "Git은 분산 버전 관리 시스템으로, 소스 코드의 변경 사항을 추적하고 여러 개발자 간의 작업을 조율하는 데 사용됩니다.", "Git이란 무엇이며 어떻게 사용하나요?", "SPRING"),
    (3, NOW(), NOW(), "React Hooks는 함수형 컴포넌트에서 상태 관리와 생명주기 기능을 사용할 수 있게 해주는 함수입니다.", "React Hooks의 주요 장점은 무엇인가요?", "REACT"),
    (4, NOW(), NOW(), "Spring Boot는 스프링 프레임워크를 기반으로 한 프로젝트 설정을 간소화하고 자동화하는 도구입니다.", "Spring Boot와 일반 Spring 프레임워크의 차이점은 무엇인가요?", "SPRING"),
    (5, NOW(), NOW(), "NoSQL 데이터베이스는 비관계형 데이터베이스로, 대용량 분산 데이터를 저장하고 처리하는 데 최적화되어 있습니다.", "NoSQL 데이터베이스의 특징과 사용 사례는 무엇인가요?", "DATABASE"),
    (6, NOW(), NOW(), "네트워크 관련된 정답인 답변 임시 데이터", "OSI 7 Layer에 대해서 설명해주세요. (임시 데이터 insert 쿼리)", "DEVELOPMENT");

SELECT CONCAT('질문 데이터: ', COUNT(*), '개 행') AS message FROM questions;

-- -------------------------------
-- 4. 면접 세션 데이터
-- -------------------------------
INSERT INTO interviews (id, member_id, start_date, created_at, updated_at)
VALUES
    (1, 1, "2025-05-01", NOW(), NOW()),
    (2, 1, "2025-05-10", NOW(), NOW()),
    (3, 1, "2025-05-13", NOW(), NOW());

SELECT CONCAT('면접 데이터: ', COUNT(*), '개 행') AS message FROM interviews;

-- -------------------------------
-- 5. 답변 데이터
-- -------------------------------
INSERT INTO answers
(id, question_id, member_id, interview_id, user_answer, video_url, running_time, comment, is_understood, aianswer, status, created_at, updated_at)
VALUES
    (1, 1, 1, 1, "IoC는 의존성 주입을 통해 구성됩니다.", "https://inpeak-bucket.s3.ap-northeast-2.amazonaws.com/videos/1/250228/inpeak-bucket+-+S3+버킷+_+S3+_+ap-northeast-2+-+Chrome+2025-03-06+11-37-30.mp4", 60, "이해 완료", true, "AI: 정답입니다.", "CORRECT", NOW(), NOW()),
    (2, 2, 1, 1, "커넥션 풀 없으면 매 요청마다 연결 생성됨.", "http://example.com/video/2.mp4", 90, "시간 오래 걸림", false, "AI: 오답입니다.", "INCORRECT", NOW(), NOW()),
    (3, 3, 1, 2, "@Service는 비즈니스 로직용입니다.", "http://example.com/video/3.mp4", 75, "", false, "AI: 정답입니다.", "CORRECT", NOW(), NOW()),
    (4, 4, 1, 2, "", "", 0, "", false, "", "SKIPPED", NOW(), NOW()),
    (5, 5, 1, 3, "useEffect는 의존성 배열 필요해요.", "http://example.com/video/5.mp4", 45, "", false, "AI: 정답입니다.", "CORRECT", NOW(), NOW()),
    (6, 6, 1, 3, "", "", 0, "", false, "", "SKIPPED", NOW(), NOW());

SELECT CONCAT('답변 데이터: ', COUNT(*), '개 행') AS message FROM answers;

-- -------------------------------
-- 6. 리프레시 토큰 데이터
-- -------------------------------
INSERT INTO refreshtokens (id, created_at, member_id, updated_at, refresh_token)
VALUES
    (1, NOW(), 1, NOW(), "test1"),
    (2, NOW(), 2, NOW(), "test2"),
    (3, NOW(), 3, NOW(), "test3"),
    (4, NOW(), 4, NOW(), "test4"),
    (5, NOW(), 5, NOW(), "test5"),
    (6, NOW(), 6, NOW(), "test6");

SELECT CONCAT('리프레시 토큰 데이터: ', COUNT(*), '개 행') AS message FROM refreshtokens;

-- -------------------------------
-- 7. 통계 데이터
-- -------------------------------
INSERT INTO member_statistics (
    member_id,
    correct_count,
    incorrect_count,
    skipped_count,
    created_at,
    updated_at
)
SELECT
    a.member_id,
    SUM(CASE WHEN a.status = 'CORRECT' THEN 1 ELSE 0 END) AS correct_count,
    SUM(CASE WHEN a.status = 'INCORRECT' THEN 1 ELSE 0 END) AS incorrect_count,
    SUM(CASE WHEN a.status = 'SKIPPED' THEN 1 ELSE 0 END) AS skipped_count,
    NOW(),
    NOW()
FROM answers a
GROUP BY a.member_id;

SELECT CONCAT('통계 데이터: ', COUNT(*), '개 행') AS message FROM member_statistics;

-- -------------------------------
-- 마무리 작업
-- -------------------------------
-- 트랜잭션 완료
COMMIT;

-- 설정 복원
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- 초기화 요약 출력
SELECT '개발 환경용 DB 초기화가 완료되었습니다.' AS message;

-- 테이블별 데이터 요약
SELECT '회원 및 관심사' AS category,
       (SELECT COUNT(*) FROM members) AS members_count,
       (SELECT COUNT(*) FROM member_interests) AS interests_count,
       (SELECT COUNT(*) FROM questions) AS questions_count,
       (SELECT COUNT(*) FROM answers) AS answers_count;
