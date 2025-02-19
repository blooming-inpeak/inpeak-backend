package com.blooming.inpeak.answer.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.member.domain.OAuth2Provider;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.domain.QuestionType;
import com.blooming.inpeak.common.config.queryDSL.QuerydslConfig;
import com.blooming.inpeak.member.domain.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.mockito.junit.jupiter.MockitoExtension;

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

        testMember = entityManager.persist(
            Member.of(
                "test@test.com",
                "test",
                "test",
                OAuth2Provider.KAKAO
            )
        );

        // QuestionType을 명시적으로 설정하여 저장
        testQuestion = entityManager.persist(Question.of("테스트 질문", QuestionType.SPRING, "최고의 답변"));

        // 인터뷰 객체 생성
        testInterview = entityManager.persist(
            Interview.of(testMember.getId(), LocalDate.now().minusDays(1)));

        // 테스트 데이터 저장
        List.of(
            createAnswer("정답1", true, 120L, AnswerStatus.CORRECT),
            createAnswer("정답2", false, 200L, AnswerStatus.CORRECT),
            createAnswer("오답1", false, 150L, AnswerStatus.INCORRECT),
            createAnswer("스킵된 답변", false, 100L, AnswerStatus.SKIPPED)
        ).forEach(entityManager::persist);

        entityManager.flush();
    }


    private Answer createAnswer(String userAnswer, boolean isUnderstood, Long runningTime,
        AnswerStatus status) {
        return Answer.builder()
            .questionId(testQuestion.getId())
            .memberId(testMember.getId())
            .interviewId(testInterview.getId())
            .userAnswer(userAnswer)
            .runningTime(runningTime)
            .isUnderstood(isUnderstood)
            .status(status)
            .build();
    }

    @Test
    @DisplayName("findAnswers()는 정답(CORRECT) 상태의 답변만 반환해야 한다.")
    void findAnswers_ShouldReturnOnlyCorrectAnswers() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Answer> results = answerRepositoryCustom.findAnswers(testMember.getId(), null,
            AnswerStatus.CORRECT, "DESC", pageable);

        // then
        assertNotNull(results);
        assertEquals(2, results.getNumberOfElements());
        assertTrue(
            results.getContent().stream().allMatch(a -> a.getStatus() == AnswerStatus.CORRECT));
    }

    @Test
    @DisplayName("findAnswers()의 ALL 조건은 INCORRECT 및 SKIPPED 상태의 답변을 반환해야 한다.")
    void findAnswers_ShouldReturnIncorrectAndSkippedAnswers_WhenStatusIsALL() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Answer> results = answerRepositoryCustom.findAnswers(testMember.getId(), null,
            AnswerStatus.ALL, "DESC", pageable);

        // then
        assertNotNull(results);
        assertEquals(2, results.getNumberOfElements());
        assertTrue(results.getContent().stream().allMatch(a ->
            a.getStatus() == AnswerStatus.INCORRECT || a.getStatus() == AnswerStatus.SKIPPED
        ));
    }

    @Test
    @DisplayName("findAnswers()는 isUnderstood가 true일 경우 이해한 답변만 반환해야 한다.")
    void findAnswers_ShouldReturnOnlyUnderstoodAnswers_WhenIsUnderstoodIsTrue() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Answer> results = answerRepositoryCustom.findAnswers(testMember.getId(), true,
            AnswerStatus.CORRECT, "DESC", pageable);

        // then
        assertNotNull(results);
        assertEquals(1, results.getNumberOfElements()); // 이해한 답변은 1개
        assertTrue(results.getContent().stream().allMatch(Answer::isUnderstood));
    }

    @Test
    @DisplayName("findAnswers()는 페이지 크기보다 하나 더 조회했을 때 hasNext가 true여야 한다.")
    void findAnswers_ShouldSetHasNextTrue_WhenMoreThanPageSizeFetched() {
        // given
        Pageable pageable = PageRequest.of(0, 1); // 페이지 크기를 1로 설정

        // when
        Slice<Answer> results = answerRepositoryCustom.findAnswers(testMember.getId(), null,
            AnswerStatus.ALL, "DESC", pageable);

        // then
        assertNotNull(results);
        assertEquals(1, results.getNumberOfElements());
        assertTrue(results.hasNext()); // 2개 이상 데이터가 있으므로 hasNext = true
    }

    @Test
    @DisplayName("findAnswers()는 정렬 조건 DESC일 때 최신 순으로 정렬해야 한다.")
    void findAnswers_ShouldReturnResultsInDescendingOrder_WhenSortTypeIsDESC() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Answer> results = answerRepositoryCustom.findAnswers(testMember.getId(), null,
            AnswerStatus.ALL, "DESC", pageable);

        // then
        assertNotNull(results);
        List<Answer> answers = results.getContent();
        assertTrue(answers.get(0).getCreatedAt().isAfter(answers.get(1).getCreatedAt())); // 최신순 확인
    }

    @Test
    @DisplayName("findAnswers()는 정렬 조건 ASC일 때 오래된 순으로 정렬해야 한다.")
    void findAnswers_ShouldReturnResultsInAscendingOrder_WhenSortTypeIsASC() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Answer> results = answerRepositoryCustom.findAnswers(testMember.getId(), null,
            AnswerStatus.ALL, "ASC", pageable);

        // then
        assertNotNull(results);
        List<Answer> answers = results.getContent();
        assertTrue(
            answers.get(0).getCreatedAt().isBefore(answers.get(1).getCreatedAt())); // 오래된 순 확인
    }

    @Test
    @DisplayName("findAnswers()는 잘못된 정렬 타입이 입력되면 IllegalArgumentException을 발생시켜야 한다.")
    void findAnswers_ShouldThrowException_WhenInvalidSortTypeProvided() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            answerRepositoryCustom.findAnswers(testMember.getId(), null, AnswerStatus.ALL,
                "INVALID_SORT", pageable);
        });
    }
}
