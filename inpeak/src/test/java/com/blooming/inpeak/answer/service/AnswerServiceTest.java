package com.blooming.inpeak.answer.service;

import static org.assertj.core.api.Assertions.*;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.answer.dto.response.AnswerDetailResponse;
import com.blooming.inpeak.answer.dto.response.AnswerListResponse;
import com.blooming.inpeak.answer.dto.response.InterviewWithAnswersResponse;
import com.blooming.inpeak.answer.dto.response.RecentAnswerListResponse;
import com.blooming.inpeak.answer.dto.response.RecentAnswerResponse;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.answer.repository.AnswerRepositoryCustom;
import com.blooming.inpeak.common.error.exception.NotFoundException;
import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.domain.QuestionType;
import com.blooming.inpeak.question.repository.QuestionRepository;
import com.blooming.inpeak.support.IntegrationTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Comparator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


class AnswerServiceTest extends IntegrationTestSupport {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private AnswerRepositoryCustom answerRepositoryCustom;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private AnswerService answerService;

    @PersistenceContext
    private EntityManager entityManager;

    private final Long memberId = 1L;


    private Answer createAnswer(Long memberId, Long questionId, Long interviewId, String userAnswer,
        Long runningTime, AnswerStatus status, boolean isUnderstood) {
        return answerRepository.save(Answer.builder()
            .questionId(questionId)
            .memberId(memberId)
            .interviewId(interviewId)
            .userAnswer(userAnswer)
            .videoURL("")
            .runningTime(runningTime)
            .comment("")
            .isUnderstood(isUnderstood)
            .status(status)
            .build());
    }

    private Answer createAnswerEntity(Long memberId, Long questionId, Long interviewId, AnswerStatus status) {
        return Answer.builder()
            .questionId(questionId)
            .memberId(memberId)
            .interviewId(interviewId)
            .userAnswer("스프링 답변1")
            .runningTime(120L)
            .isUnderstood(false)
            .status(status)
            .build();
    }

    @DisplayName("저장된 데이터를 기반으로 올바른 응답을 반환해야 한다")
    @Transactional
    @Test
    void getAnswerList_ShouldReturnCorrectResults() {
        // given
        Interview interview = interviewRepository.save(Interview.of(memberId, LocalDate.now()));
        Question question1 = questionRepository.save(
            Question.of("자바의 GC 동작 방식", QuestionType.SPRING, "모법 답변"));
        Question question2 = questionRepository.save(
            Question.of("Spring DI의 원리", QuestionType.SPRING, "모법 답변"));

        createAnswer(memberId, question1.getId(), interview.getId(), "GC는 어쩌고 저쩌고", 120L,
            AnswerStatus.CORRECT, true);
        createAnswer(memberId, question2.getId(), interview.getId(), "Spring의 DI 원리가 어쩌고", 130L,
            AnswerStatus.INCORRECT, false);

        AnswerFilterCommand command = new AnswerFilterCommand(memberId, "DESC", true,
            AnswerStatus.CORRECT, 0, 5);

        entityManager.flush();
        entityManager.clear();

        // when
        AnswerListResponse response = answerService.getAnswerList(command);

        // then
        assertThat(response).isNotNull();
        assertThat(response.AnswerResponseList()).hasSize(1);
    }

    @DisplayName("getAnswerList()는 조회 결과가 없을 경우 빈 리스트를 반환해야 한다.")
    @Transactional
    @Test
    void getAnswerList_ShouldReturnEmptyList_WhenNoResults() {
        // given
        AnswerFilterCommand command = new AnswerFilterCommand(memberId, "DESC", true,
            AnswerStatus.CORRECT, 0, 5);

        // when
        AnswerListResponse response = answerService.getAnswerList(command);

        // then
        assertThat(response).isNotNull();
        assertThat(response.AnswerResponseList()).isEmpty();
        assertThat(response.hasNext()).isFalse();
    }

    @DisplayName("skipAnswer()는 사용자의 답변을 스킵 상태로 저장해야 한다.")
    @Transactional
    @Test
    void skipAnswer_ShouldSaveSkippedAnswer() {
        // given
        Long interviewId = interviewRepository.save(Interview.of(memberId, LocalDate.now()))
            .getId();
        Question question = questionRepository.save(
            Question.of("자바의 GC 동작 방식", QuestionType.SPRING, "모법 답변"));

        // when
        answerService.skipAnswer(memberId, question.getId(), interviewId);

        // then
        List<Answer> answers = answerRepository.findAll();
        assertThat(answers).hasSize(1);
        assertThat(answers.get(0).getStatus()).isEqualTo(AnswerStatus.SKIPPED);
    }

    @DisplayName("getAnswersByDate()는 특정 날짜에 진행된 인터뷰 정보와 답변 리스트를 반환해야 한다.")
    @Transactional
    @Test
    void getAnswersByDate_ShouldReturnInterviewWithAnswers() {
        // given
        LocalDate date = LocalDate.of(2025, 2, 15);
        Interview interview = interviewRepository.save(Interview.of(memberId, date));
        Question question1 = questionRepository.save(
            Question.of("자바의 GC 동작 방식", QuestionType.SPRING, "모법 답변"));
        Question question2 = questionRepository.save(
            Question.of("Spring DI의 원리", QuestionType.SPRING, "모법 답변"));

        createAnswer(memberId, question1.getId(), interview.getId(), "GC는 어쩌고 저쩌고", 120L,
            AnswerStatus.CORRECT, true);
        createAnswer(memberId, question2.getId(), interview.getId(), "Spring의 DI 원리가 어쩌고", 130L,
            AnswerStatus.INCORRECT, false);

        entityManager.flush();
        entityManager.clear();

        // when
        InterviewWithAnswersResponse response = answerService.getAnswersByDate(memberId, date);

        // then
        assertThat(response).isNotNull();
        assertThat(response.startDate()).isEqualTo(date);
        assertThat(response.answers()).hasSize(2);
    }

    @DisplayName("getRecentAnswers()는 필터 조건에 따라 최대 3개의 최신순 답변 리스트를 반환해야 한다.")
    @Transactional
    @Test
    void getRecentAnswers_ShouldReturnUpToThreeLatestFilteredAnswers() {
        // given
        Interview interview = interviewRepository.save(Interview.of(memberId, LocalDate.now()));

        Question question1 = questionRepository.save(
            Question.of("스프링 질문1", QuestionType.SPRING, "모법 답변"));
        Question question2 = questionRepository.save(
            Question.of("스프링 질문2", QuestionType.SPRING, "모법 답변"));
        Question question3 = questionRepository.save(
            Question.of("스프링 질문3", QuestionType.SPRING, "모법 답변"));
        Question question4 = questionRepository.save(
            Question.of("스프링 질문4", QuestionType.SPRING, "모법 답변"));

        createAnswer(memberId, question1.getId(), interview.getId(), "스프링 답변1", 120L,
            AnswerStatus.CORRECT, true);
        createAnswer(memberId, question2.getId(), interview.getId(), "스프링 답변2", 130L,
            AnswerStatus.INCORRECT, false);
        createAnswer(memberId, question3.getId(), interview.getId(), "스프링 답변3", 120L,
            AnswerStatus.CORRECT, false);
        createAnswer(memberId, question4.getId(), interview.getId(), "스프링 답변4", 120L,
            AnswerStatus.CORRECT, true);

        entityManager.flush();
        entityManager.clear();

        // when
        RecentAnswerListResponse response = answerService.getRecentAnswers(memberId,
            AnswerStatus.CORRECT);

        // then
        // 최대 3개의 답변만 반환해야 함
        assertThat(response).isNotNull();
        assertThat(response.recentAnswers()).hasSize(3);

        // 필터 조건에 맞는 답변만 반환해야 함
        assertThat(response.recentAnswers())
            .extracting(RecentAnswerResponse::answerStatus)
            .containsOnly(AnswerStatus.CORRECT);

        // 최신순으로 정렬되어야 함
        List<Long> answerIds = response.recentAnswers()
            .stream()
            .map(RecentAnswerResponse::answerId)
            .toList();
        assertThat(answerIds).isSortedAccordingTo(Comparator.reverseOrder());
    }

    @DisplayName("updateUnderstood()는 사용자의 이해 여부를 업데이트해야 한다.")
    @Transactional
    @Test
    void updateUnderstood_ShouldUpdateAnswerStatus() {
        // given
        Long memberId = 1L;
        Interview interview = interviewRepository.save(Interview.of(memberId, LocalDate.now()));
        Question question = questionRepository.save(
            Question.of("자바의 GC 동작 방식", QuestionType.SPRING, "모범 답변"));

        createAnswer(memberId, question.getId(), interview.getId(), "GC에 대한 설명", 120L,
            AnswerStatus.CORRECT, false);

        entityManager.flush();
        entityManager.clear();

        Answer answer = answerRepository.findAll().get(0);

        // when
        answerService.updateUnderstood(answer.getId(), true, memberId); // ← 변경된 부분

        // then
        Answer updatedAnswer = answerRepository.findById(answer.getId()).orElseThrow();
        assertThat(updatedAnswer.isUnderstood()).isTrue();
    }


    @DisplayName("존재하지 않는 답변 ID로 updateUnderstood()를 호출하면 NotFoundException이 발생해야 한다.")
    @Transactional
    @Test
    void updateUnderstood_ShouldThrowException_WhenAnswerNotFound() {
        // given
        Long nonExistingId = 9999L;
        Long memberId = 1L;

        // when & then
        assertThatThrownBy(() -> answerService.updateUnderstood(nonExistingId, true, memberId)) // ← memberId 추가
            .isInstanceOf(NotFoundException.class)
            .hasMessage("해당 답변이 존재하지 않습니다.");
    }


    @DisplayName("updateComment()는 답변의 코멘트를 정상적으로 변경해야 한다.")
    @Transactional
    @Test
    void updateComment_ShouldUpdateCommentSuccessfully() {
        // given
        Long memberId = 1L;
        String originalComment = "이해가 안 가는 부분이 있어요.";
        String updatedComment = "이해가 안 가는 부분이 있어요. 더 설명해주세요.";

        Interview interview = interviewRepository.save(Interview.of(memberId, LocalDate.now()));
        Question question = questionRepository.save(
            Question.of("자바의 GC 동작 방식", QuestionType.SPRING, "모범 답변"));

        Answer answer = createAnswer(memberId, question.getId(), interview.getId(), "GC에 대한 설명",
            120L,
            AnswerStatus.CORRECT, false);

        answer.setComment(originalComment);

        entityManager.flush();
        entityManager.clear();

        Answer newAnswer = answerRepository.findAll().get(0);

        // when
        answerService.updateComment(newAnswer.getId(), updatedComment);

        // then
        Answer updatedAnswer = answerRepository.findById(newAnswer.getId()).orElseThrow();
        assertThat(updatedAnswer.getComment()).isEqualTo(updatedComment);
    }

    @DisplayName("updateComment()는 존재하지 않는 답변 ID로 요청하면 예외를 발생시켜야 한다.")
    @Transactional
    @Test
    void updateComment_ShouldThrowException_WhenAnswerNotFound() {
        // given
        Long nonExistingId = 9999L;
        String comment = "개발 힘들다..";

        // when & then
        assertThatThrownBy(() -> answerService.updateComment(nonExistingId, comment))
            .isInstanceOf(NotFoundException.class) // ✅ 변경됨
            .hasMessage("해당 답변이 존재하지 않습니다.");
    }


    @DisplayName("getAnswer()는 interviewId, questionId, memberId로 답변을 조회하면 올바른 응답을 반환해야 한다.")
    @Transactional
    @Test
    void getAnswer_ShouldReturnCorrectAnswer() {
        // given
        Long memberId = 1L;
        Interview interview = interviewRepository.save(Interview.of(memberId, LocalDate.now()));
        Question question = questionRepository.save(
            Question.of("Spring의 IoC 컨테이너란?", QuestionType.SPRING, "IoC 컨테이너는 Bean을 관리합니다.")
        );

        Answer answer = createAnswer(memberId, question.getId(), interview.getId(),
            "IoC는 제어의 역전", 30L, AnswerStatus.CORRECT, true);

        entityManager.flush();
        entityManager.clear();

        // when
        AnswerDetailResponse response = answerService.getAnswer(interview.getId(), question.getId(), memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.userAnswer()).isEqualTo("IoC는 제어의 역전");
        assertThat(response.answerStatus()).isEqualTo(AnswerStatus.CORRECT);
    }

    @DisplayName("getAnswer()는 존재하지 않는 답변이면 NotFoundException을 발생시켜야 한다.")
    @Transactional
    @Test
    void getAnswer_ShouldThrowException_WhenAnswerNotFound() {
        // given
        Long fakeInterviewId = 9999L;
        Long fakeQuestionId = 8888L;
        Long memberId = 1L;

        // when & then
        assertThatThrownBy(() -> answerService.getAnswer(fakeInterviewId, fakeQuestionId, memberId))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("해당 답변이 존재하지 않습니다.");
    }
}
