package com.blooming.inpeak.answer.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.domain.QuestionType;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.common.config.queryDSL.QuerydslConfig;
import com.blooming.inpeak.member.domain.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
@Import(QuerydslConfig.class)
class AnswerRepositoryCustomTest {

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private TestEntityManager entityManager;

    private AnswerRepositoryCustom answerRepositoryCustom;
    private Member testMember;
    private Question testQuestion;
    private Interview testInterview;

    @BeforeEach
    void setUp() {
        answerRepositoryCustom = new AnswerRepositoryCustom(queryFactory);

        testMember = entityManager.persist(Member.of("testUser", "testAccessToken"));

        testQuestion = entityManager.persist(Question.of("테스트 질문", QuestionType.SPRING, "정답 예시"));

        testInterview = entityManager.persist(Interview.of(testMember.getId(), LocalDate.now().minusDays(1)));

        // ✅ Answer 생성
        List.of(
            createAnswer("정답1", true, 120L, AnswerStatus.CORRECT),
            createAnswer("정답2", false, 200L, AnswerStatus.CORRECT),
            createAnswer("틀린 답변", false, 150L, AnswerStatus.INCORRECT)
        ).forEach(entityManager::persist);

        entityManager.flush();
    }

    /**
     * Answer 객체 생성 헬퍼 메서드
     */
    private Answer createAnswer(String userAnswer, boolean isUnderstood, Long runningTime, AnswerStatus status) {
        return Answer.builder()
            .questionId(testQuestion.getId())
            .memberId(testMember.getId())
            .interviewId(testInterview.getId())
            .userAnswer(userAnswer)
            .videoURL("")
            .runningTime(runningTime)
            .comment("")
            .isUnderstood(isUnderstood)
            .status(status)
            .build();
    }

    @Test
    @DisplayName("findCorrectAnswerList()는 CORRECT 상태의 답변만 반환해야 한다.")
    void findCorrectAnswerList_ShouldReturnOnlyCorrectAnswers() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Answer> results = answerRepositoryCustom.findCorrectAnswerList(testMember.getId(), false, "DESC", pageable);

        // then
        assertNotNull(results);
        assertEquals(2, results.getNumberOfElements()); // CORRECT 상태 2개만 반환
        assertTrue(results.getContent().stream().allMatch(a -> a.getStatus() == AnswerStatus.CORRECT));
    }

    @Test
    @DisplayName("findCorrectAnswerList()는 isUnderstood = true일 때 이해된 답변만 반환해야 한다.")
    void findCorrectAnswerList_ShouldReturnOnlyUnderstoodAnswers_WhenIsUnderstoodTrue() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Answer> results = answerRepositoryCustom.findCorrectAnswerList(testMember.getId(), true, "DESC", pageable);

        // then
        assertNotNull(results);
        assertEquals(1, results.getNumberOfElements()); // 이해한 답변 1개만 반환
        assertTrue(results.getContent().stream().allMatch(Answer::isUnderstood));
    }

    @Test
    @DisplayName("findCorrectAnswerList()는 페이지 크기보다 하나 더 조회했을 때 hasNext가 true여야 한다.")
    void findCorrectAnswerList_ShouldSetHasNextTrue_WhenMoreThanPageSizeFetched() {
        // given
        Pageable pageable = PageRequest.of(0, 1); // 페이지 크기를 2로 설정

        // when
        Slice<Answer> results = answerRepositoryCustom.findCorrectAnswerList(testMember.getId(), false, "DESC", pageable);

        // then
        assertNotNull(results);
        assertEquals(1, results.getNumberOfElements());
        assertTrue(results.hasNext()); // 2개 이상 데이터가 있으므로 hasNext = true
    }

    @Test
    @DisplayName("findCorrectAnswerList()는 정렬 조건 DESC일 때 최신 순으로 정렬해야 한다.")
    void findCorrectAnswerList_ShouldReturnResultsInDescendingOrder_WhenSortTypeIsDESC() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Answer> results = answerRepositoryCustom.findCorrectAnswerList(testMember.getId(), false, "DESC", pageable);

        // then
        assertNotNull(results);
        List<Answer> answers = results.getContent();
        assertTrue(answers.get(0).getCreatedAt().isAfter(answers.get(1).getCreatedAt())); // 최신순 확인
    }

    @Test
    @DisplayName("findCorrectAnswerList()는 정렬 조건 ASC일 때 오래된 순으로 정렬해야 한다.")
    void findCorrectAnswerList_ShouldReturnResultsInAscendingOrder_WhenSortTypeIsASC() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Answer> results = answerRepositoryCustom.findCorrectAnswerList(testMember.getId(), false, "ASC", pageable);

        // then
        assertNotNull(results);
        List<Answer> answers = results.getContent();
        assertTrue(answers.get(0).getCreatedAt().isBefore(answers.get(1).getCreatedAt())); // 오래된 순 확인
    }

    @Test
    @DisplayName("findCorrectAnswerList()는 잘못된 정렬 타입이 입력되면 IllegalArgumentException을 발생시켜야 한다.")
    void findCorrectAnswerList_ShouldThrowException_WhenInvalidSortTypeProvided() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            answerRepositoryCustom.findCorrectAnswerList(testMember.getId(), false, "INVALID_SORT", pageable);
        });
    }
}
