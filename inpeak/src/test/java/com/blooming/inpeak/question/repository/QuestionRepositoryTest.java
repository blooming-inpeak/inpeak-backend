package com.blooming.inpeak.question.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.domain.QuestionType;
import com.blooming.inpeak.support.IntegrationTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("QuestionRepository 테스트")
class QuestionRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    private static final Long MEMBER_ID = 1L;
    private Long interviewId;

    @BeforeEach
    void setUp() {
        interviewId = interviewRepository.save(Interview.of(MEMBER_ID, LocalDate.now())).getId();
    }

    @Test
    @Transactional
    @DisplayName("질문을 조회할 때, 회원이 관심있는 분야의 질문만 조회해야 한다.")
    void findFilteredQuestionsByTypes_ShouldReturnQuestionsForUserInterests() {
        // given
        List<String> types = List.of(QuestionType.REACT.name());
        Question q1 = questionRepository.save(
            Question.of("React 질문", QuestionType.REACT, "리액트 정답"));
        Question q2 = questionRepository.save(
            Question.of("Spring 질문", QuestionType.SPRING, "스프링 정답"));
        Question q3 = questionRepository.save(
            Question.of("DB 질문", QuestionType.DATABASE, "DB 정답"));

        // when
        List<Question> result = questionRepository.findFilteredQuestionsByTypes(types, MEMBER_ID);

        // then: q1만 조회되어야 함
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(q1.getId());
        assertThat(result.getFirst().getContent()).isEqualTo(q1.getContent());
    }

    @Test
    @Transactional
    @DisplayName("질문을 조회할 때, 완전히 이해한 질문을 제외하고 조회해야 한다.")
    void findFilteredQuestionsByTypes_ShouldNotReturnUnderstoodQuestions() {
        // given
        List<String> types = List.of(QuestionType.REACT.name(), QuestionType.SPRING.name());
        Question q1 = questionRepository.save(
            Question.of("React 질문", QuestionType.REACT, "리액트 정답"));
        Question q2 = questionRepository.save(
            Question.of("Spring 질문", QuestionType.SPRING, "스프링 정답"));

        Answer a1 = answerRepository.save(
            Answer.builder()
                .questionId(q1.getId())
                .memberId(MEMBER_ID)
                .interviewId(interviewId)
                .userAnswer("리액트 답변")
                .isUnderstood(true) // 이해함
                .status(AnswerStatus.CORRECT)
                .build()
        );

        Answer a2 = answerRepository.save(
            Answer.builder()
                .questionId(q2.getId())
                .memberId(MEMBER_ID)
                .interviewId(interviewId)
                .userAnswer("스프링 답변")
                .isUnderstood(false) // 이해하지 못함
                .status(AnswerStatus.CORRECT)
                .build()
        );

        // when
        List<Question> result = questionRepository.findFilteredQuestionsByTypes(types, MEMBER_ID);

        // then: q2만 조회되어야 함
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(q2.getId());
        assertThat(result.getFirst().getContent()).isEqualTo(q2.getContent());
    }
}
