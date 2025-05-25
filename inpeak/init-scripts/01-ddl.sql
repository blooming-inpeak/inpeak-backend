-- 01-schema.sql
-- 개발 환경용 스키마 정의 스크립트

-- 인코딩 설정
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 오류 처리를 위한 구성
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE;
SET SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- 디버깅을 위한 로그 출력
SELECT '개발 환경용 스키마 초기화를 시작합니다.' AS message;

-- 트랜잭션 시작
START TRANSACTION;

-- 테이블 삭제 (의존성 역순)
DROP TABLE IF EXISTS answers;
DROP TABLE IF EXISTS interviews;
DROP TABLE IF EXISTS refreshtokens;
DROP TABLE IF EXISTS member_interests;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS members;

-- 테이블 생성
CREATE TABLE members (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         kakao_id BIGINT,
                         nickname VARCHAR(255) NOT NULL UNIQUE,
                         total_question_count BIGINT,
                         correct_answer_count BIGINT,
                         provider VARCHAR(50) NOT NULL,
                         registration_status VARCHAR(50) NOT NULL,
                         kakao_email VARCHAR(255) NOT NULL,
                         created_at TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP NOT NULL
);

CREATE TABLE questions (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           content TEXT NOT NULL,
                           type VARCHAR(50) NOT NULL,
                           best_answer TEXT NOT NULL,
                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP NOT NULL
);

CREATE TABLE member_interests (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  member_id BIGINT NOT NULL,
                                  interest_type VARCHAR(50) NOT NULL
);

CREATE TABLE interviews (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            member_id BIGINT NOT NULL,
                            start_date DATE NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP NOT NULL
);

CREATE TABLE answers (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         question_id BIGINT NOT NULL,
                         member_id BIGINT NOT NULL,
                         interview_id BIGINT NOT NULL,
                         user_answer TEXT,
                         video_url VARCHAR(255),
                         running_time BIGINT,
                         comment TEXT,
                         is_understood BOOLEAN NOT NULL DEFAULT FALSE,
                         aianswer TEXT,
                         status VARCHAR(50) NOT NULL,
                         created_at TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP NOT NULL
);

CREATE TABLE refreshtokens (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               member_id BIGINT NOT NULL,
                               refresh_token VARCHAR(255) NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP NOT NULL
);

CREATE TABLE member_statistics (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    member_id BIGINT NOT NULL UNIQUE,
                                    correct_count INT NOT NULL DEFAULT 0,
                                    incorrect_count INT NOT NULL DEFAULT 0,
                                    skipped_count INT NOT NULL DEFAULT 0,
                                    total_count INT GENERATED ALWAYS AS (correct_count + incorrect_count + skipped_count) STORED,
                                    created_at TIMESTAMP NOT NULL,
                                    updated_at TIMESTAMP NOT NULL
);

-- 트랜잭션 완료
COMMIT;

-- 설정 복원
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

SELECT '스키마 초기화가 완료되었습니다.' AS message;
