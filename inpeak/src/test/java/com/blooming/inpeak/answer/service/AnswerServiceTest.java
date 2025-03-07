package com.blooming.inpeak.answer.service;

import static org.assertj.core.api.Assertions.*;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.answer.dto.response.AnswerListResponse;
import com.blooming.inpeak.answer.dto.response.InterviewWithAnswersResponse;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.answer.repository.AnswerRepositoryCustom;
import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.domain.QuestionType;
import com.blooming.inpeak.question.repository.QuestionRepository;
import com.blooming.inpeak.support.IntegrationTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    private Long memberId;
    private AnswerFilterCommand command;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        command = new AnswerFilterCommand(memberId, "DESC", true, AnswerStatus.CORRECT, 0, 5);
    }

    private void createAnswer(Long memberId, Long questionId, Long interviewId, String userAnswer,
        int runningTime, AnswerStatus status, boolean isUnderstood) {
        answerRepository.save(Answer.builder()
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

        createAnswer(memberId, question1.getId(), interview.getId(), "GC는 어쩌고 저쩌고", 120,
            AnswerStatus.CORRECT, true);
        createAnswer(memberId, question2.getId(), interview.getId(), "Spring의 DI 원리가 어쩌고", 130,
            AnswerStatus.INCORRECT, false);

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

        createAnswer(memberId, question1.getId(), interview.getId(), "GC는 어쩌고 저쩌고", 120,
            AnswerStatus.CORRECT, true);
        createAnswer(memberId, question2.getId(), interview.getId(), "Spring의 DI 원리가 어쩌고", 130,
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

    @DisplayName("updateUnderstood()는 사용자의 이해 여부를 업데이트해야 한다.")
    @Transactional
    @Test
    void updateUnderstood_ShouldUpdateAnswerStatus() {
        // given
        Long memberId = 1L;
        Interview interview = interviewRepository.save(Interview.of(memberId, LocalDate.now()));
        Question question = questionRepository.save(
            Question.of("자바의 GC 동작 방식", QuestionType.SPRING, "모범 답변"));

        createAnswer(memberId, question.getId(), interview.getId(), "GC에 대한 설명", 120,
            AnswerStatus.CORRECT, false);

        entityManager.flush();
        entityManager.clear();

        Answer answer = answerRepository.findAll().get(0);

        // when
        answerService.updateUnderstood(answer.getId(), true);

        // then
        Answer updatedAnswer = answerRepository.findById(answer.getId()).orElseThrow();
        assertThat(updatedAnswer.isUnderstood()).isTrue();
    }

    @DisplayName("존재하지 않는 답변 ID로 updateUnderstood()를 호출하면 예외가 발생해야 한다.")
    @Transactional
    @Test
    void updateUnderstood_ShouldThrowException_WhenAnswerNotFound() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThatThrownBy(() -> answerService.updateUnderstood(nonExistingId, true))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("해당 답변이 존재하지 않습니다.");
    }
}
