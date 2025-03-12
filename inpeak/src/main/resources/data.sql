insert into members
(correct_answer_count, created_at, id, total_question_count,
 updated_at, access_token, email, nickname, provider, registration_status)
values
    (0, now(), 1, 0, now(), 'aaaaaaaaa', 'test1@test.com', 'test1', 'KAKAO', 'COMPLETED'),
    (0, now(), 2, 0, now(), 'bbbbbbbbb', 'test2@test.com', 'test2', 'KAKAO', 'INITIATED'),
    (0, now(), 3, 0, now(), 'ccccccccc', 'test3@test.com', 'test3', 'KAKAO', 'COMPLETED'),
    (0, now(), 4, 0, now(), 'ddddddddd', 'test4@test.com', 'test4', 'KAKAO', 'INITIATED'),
    (0, now(), 5, 0, now(), 'eeeeeeeee', 'test5@test.com', 'test5', 'KAKAO', 'COMPLETED'),
    (0, now(), 6, 0, now(), 'fffffffff', 'test6@test.com', 'test6', 'KAKAO', 'INITIATED');
