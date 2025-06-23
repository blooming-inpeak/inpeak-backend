package com.blooming.inpeak.answer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.domain.QuestionType;
import com.blooming.inpeak.question.repository.QuestionRepository;
import com.blooming.inpeak.support.IntegrationTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AnswerRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Long memberId = 1L;
    private LocalDate interviewDate = LocalDate.of(2024, 2, 20);
    private Interview interview;
    private Question question;

    @BeforeEach
    void setUp() {
        interview = createInterview(memberId, interviewDate);
        question = createQuestion("Spring의 IoC 컨테이너란?", QuestionType.SPRING, "IoC 컨테이너는 Bean을 관리합니다.");

        saveAnswer(question.getId(), memberId, interview.getId(), "IoC는 제어의 역전", AnswerStatus.CORRECT, 30, true);
        saveAnswer(question.getId(), memberId, interview.getId(), "IoC는 데이터 바인딩과 관련 있다", AnswerStatus.INCORRECT, 20, false);
        saveSkippedAnswer(memberId, question.getId(), interview.getId());

        clearPersistenceContext();
    }

    private Interview createInterview(Long memberId, LocalDate date) {
        return interviewRepository.save(Interview.of(memberId, date));
    }

    private Question createQuestion(String text, QuestionType type, String answer) {
        return questionRepository.save(Question.of(text, type, answer));
    }

    private void saveAnswer(Long questionId, Long memberId, Long interviewId, String userAnswer, AnswerStatus status, long runningTime, boolean isUnderstood) {
        answerRepository.save(Answer.builder()
            .questionId(questionId)
            .memberId(memberId)
            .interviewId(interviewId)
            .userAnswer(userAnswer)
            .status(status)
            .runningTime(runningTime)
            .isUnderstood(isUnderstood)
            .build());
    }

    private void saveSkippedAnswer(Long memberId, Long questionId, Long interviewId) {
        answerRepository.save(Answer.ofSkipped(memberId, questionId, interviewId));
    }

    private void clearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("특정 멤버가 특정 날짜에 진행한 인터뷰의 답변 리스트를 조회한다.")
    void findAnswersByMemberAndDate() {
        List<Answer> answers = answerRepository.findAnswersByMemberAndDate(memberId, interviewDate);

        assertThat(answers).hasSize(3);
        assertThat(answers.get(0).getInterview().getMemberId()).isEqualTo(memberId);
        assertThat(answers.get(1).getInterview().getStartDate()).isEqualTo(interviewDate);
        assertThat(answers.get(0).getQuestion().getType()).isEqualTo(QuestionType.SPRING);

        assertThat(answers).extracting(Answer::getStatus)
            .containsExactlyInAnyOrder(AnswerStatus.CORRECT, AnswerStatus.INCORRECT, AnswerStatus.SKIPPED);
    }
}
