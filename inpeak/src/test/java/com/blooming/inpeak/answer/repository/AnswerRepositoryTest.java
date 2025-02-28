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
        // Given: 인터뷰 생성
        interview = Interview.of(memberId, interviewDate);
        interview = interviewRepository.save(interview);

        // Given: 질문 생성 (QuestionType 반영)
        question = Question.of("Spring의 IoC 컨테이너란?", QuestionType.SPRING, "IoC 컨테이너는 Bean을 관리합니다.");
        question = questionRepository.save(question);

        // Given: 인터뷰와 질문을 기반으로 답변 데이터 추가 (AnswerStatus 반영)
        Answer correctAnswer = Answer.builder()
            .questionId(question.getId())
            .memberId(memberId)
            .interviewId(interview.getId())
            .userAnswer("IoC는 제어의 역전")
            .status(AnswerStatus.CORRECT)
            .isUnderstood(true)
            .build();

        Answer incorrectAnswer = Answer.builder()
            .questionId(question.getId())
            .memberId(memberId)
            .interviewId(interview.getId())
            .userAnswer("IoC는 데이터 바인딩과 관련 있다")
            .status(AnswerStatus.INCORRECT)
            .isUnderstood(false)
            .build();

        Answer skippedAnswer = Answer.ofSkipped(memberId, question.getId(), interview.getId());

        answerRepository.save(correctAnswer);
        answerRepository.save(incorrectAnswer);
        answerRepository.save(skippedAnswer);

        // 영속성 컨텍스트 초기화
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("특정 멤버가 특정 날짜에 진행한 인터뷰의 답변 리스트를 조회한다.")
    void findAnswersByMemberAndDate() {
        // When: 특정 멤버의 특정 날짜 인터뷰 답변 조회
        List<Answer> answers = answerRepository.findAnswersByMemberAndDate(memberId, interviewDate);

        // Then: 결과 검증
        assertThat(answers).hasSize(3); // 3개의 답변이 조회되어야 함
        assertThat(answers.get(0).getInterview().getMemberId()).isEqualTo(memberId);
        assertThat(answers.get(1).getInterview().getStartDate()).isEqualTo(interviewDate);
        assertThat(answers.get(0).getQuestion().getType()).isEqualTo(QuestionType.SPRING);

        // AnswerStatus 검증
        assertThat(answers).extracting(Answer::getStatus)
            .containsExactlyInAnyOrder(AnswerStatus.CORRECT, AnswerStatus.INCORRECT, AnswerStatus.SKIPPED);
    }
}
