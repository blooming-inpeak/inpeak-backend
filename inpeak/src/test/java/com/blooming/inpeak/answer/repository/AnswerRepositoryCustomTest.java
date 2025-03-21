package com.blooming.inpeak.answer.repository;

import static org.assertj.core.api.Assertions.*;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.member.domain.OAuth2Provider;
import com.blooming.inpeak.member.domain.RegistrationStatus;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.domain.QuestionType;
import com.blooming.inpeak.common.config.queryDSL.QuerydslConfig;
import com.blooming.inpeak.member.domain.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.Comparator;
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
                1234567890L,
                "테스트 닉네임",
                "test@test.com",
                OAuth2Provider.KAKAO,
                RegistrationStatus.COMPLETED
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
        assertThat(results).isNotNull();
        assertThat(results.getNumberOfElements()).isEqualTo(2);
        assertThat(results.getContent()).allMatch(a -> a.getStatus() == AnswerStatus.CORRECT);
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
        assertThat(results).isNotNull();
        assertThat(results.getNumberOfElements()).isEqualTo(2);
        assertThat(results.getContent())
            .extracting(Answer::getStatus)
            .containsOnly(AnswerStatus.INCORRECT, AnswerStatus.SKIPPED);
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
        assertThat(results).isNotNull();
        assertThat(results.getNumberOfElements()).isEqualTo(1);
        assertThat(results.getContent()).allMatch(Answer::isUnderstood);
    }

    @Test
    @DisplayName("findAnswers()는 페이지 크기보다 하나 더 조회했을 때 hasNext가 true여야 한다.")
    void findAnswers_ShouldSetHasNextTrue_WhenMoreThanPageSizeFetched() {
        // given
        Pageable pageable = PageRequest.of(0, 1);

        // when
        Slice<Answer> results = answerRepositoryCustom.findAnswers(testMember.getId(), null,
            AnswerStatus.ALL, "DESC", pageable);

        // then
        assertThat(results).isNotNull();
        assertThat(results.getNumberOfElements()).isEqualTo(1);
        assertThat(results.hasNext()).isTrue();
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
        assertThat(results).isNotNull();
        assertThat(results.getContent()).isSortedAccordingTo(Comparator.comparing(Answer::getCreatedAt).reversed());
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
        assertThat(results).isNotNull();
        assertThat(results.getContent()).isSortedAccordingTo(Comparator.comparing(Answer::getCreatedAt));
    }

    @Test
    @DisplayName("findAnswers()는 잘못된 정렬 타입이 입력되면 IllegalArgumentException을 발생시켜야 한다.")
    void findAnswers_ShouldThrowException_WhenInvalidSortTypeProvided() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> answerRepositoryCustom.findAnswers(testMember.getId(), null, AnswerStatus.ALL,
            "INVALID_SORT", pageable))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("findRecentAnswers()는 최대 3개의 답변을 반환해야 한다.")
    void findRecentAnswers_ShouldReturnAtMostThreeAnswers() {
        // when
        List<Answer> results = answerRepositoryCustom.findRecentAnswers(testMember.getId(), AnswerStatus.ALL);

        // then
        assertThat(results).isNotNull();
        assertThat(results).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("findRecentAnswers()는 필터 조건이 ALL일 때 모든 조건의 답변을 반환해야 한다.")
    void findRecentAnswers_ShouldReturnAllAnswers_WhenStatusIsALL() {
        // when
        List<Answer> results = answerRepositoryCustom.findRecentAnswers(testMember.getId(), AnswerStatus.ALL);

        // then
        assertThat(results).isNotNull();
        assertThat(results).extracting(Answer::getStatus)
            .containsAnyOf(AnswerStatus.CORRECT, AnswerStatus.INCORRECT, AnswerStatus.SKIPPED);

    }

    @Test
    @DisplayName("findRecentAnswers()는 필터 조건이 CORRECT일 때 정답 상태의 답변만 반환해야 한다.")
    void findRecentAnswers_ShouldReturnCorrectAnswers_WhenStatusIsCORRECT() {
        // when
        List<Answer> results = answerRepositoryCustom.findRecentAnswers(testMember.getId(), AnswerStatus.CORRECT);

        // then
        assertThat(results).isNotNull();
        assertThat(results).extracting(Answer::getStatus)
            .containsOnly(AnswerStatus.CORRECT);
    }

    @Test
    @DisplayName("findRecentAnswers()는 필터 조건이 INCORRECT일 때 오답 상태의 답변만 반환해야 한다.")
    void findRecentAnswers_ShouldReturnIncorrectAnswers_WhenStatusIsINCORRECT() {
        // when
        List<Answer> results = answerRepositoryCustom.findRecentAnswers(testMember.getId(), AnswerStatus.INCORRECT);

        // then
        assertThat(results).isNotNull();
        assertThat(results).extracting(Answer::getStatus)
            .containsOnly(AnswerStatus.INCORRECT);
    }

    @Test
    @DisplayName("findRecentAnswers()는 필터 조건이 SKIPPED일 때 스킵 상태의 답변만 반환해야 한다.")
    void findRecentAnswers_ShouldReturnSkippedAnswers_WhenStatusIsSKIPPED() {
        // when
        List<Answer> results = answerRepositoryCustom.findRecentAnswers(testMember.getId(), AnswerStatus.SKIPPED);

        // then
        assertThat(results).isNotNull();
        assertThat(results).extracting(Answer::getStatus)
            .containsOnly(AnswerStatus.SKIPPED);
    }

    @Test
    @DisplayName("findRecentAnswers()는 최신순으로 정렬된 답변을 반환해야 한다.")
    void findRecentAnswers_ShouldReturnAnswersInDescendingOrder() {
        // when
        List<Answer> results = answerRepositoryCustom.findRecentAnswers(testMember.getId(), AnswerStatus.ALL);

        // then
        assertThat(results).isNotNull();
        assertThat(results).isSortedAccordingTo(Comparator.comparing(Answer::getCreatedAt).reversed());
    }

    @Test
    @DisplayName("findAnswers()는 isUnderstood가 false일 경우 이해하지 못한 정답만 반환해야 한다.")
    void findAnswers_ShouldReturnOnlyNotUnderstoodCorrectAnswers_WhenIsUnderstoodIsFalse() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Answer> results = answerRepositoryCustom.findAnswers(testMember.getId(), false,
            AnswerStatus.CORRECT, "DESC", pageable);

        // then
        assertThat(results).isNotNull();
        assertThat(results.getNumberOfElements()).isEqualTo(1); // 이해하지 못한 정답은 1개
        assertThat(results.getContent()).allMatch(answer ->
            !answer.isUnderstood() && answer.getStatus() == AnswerStatus.CORRECT);
    }

}
