package com.blooming.inpeak.question.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import com.blooming.inpeak.member.domain.InterestType;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.domain.QuestionType;
import com.blooming.inpeak.question.dto.response.QuestionResponse;
import com.blooming.inpeak.question.repository.QuestionRepository;
import com.blooming.inpeak.support.IntegrationTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("QuestionService 테스트")
public class QuestionServiceTest extends IntegrationTestSupport {

    @Autowired
    private QuestionService questionService;

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
    @DisplayName("회원 관심사에 따른 질문 조회 시, 이미 이해한 질문은 제외하고 최대 3개만 반환해야 한다.")
    void getFilteredQuestions_excludesUnderstoodQuestions_andReturnsUpTo3() {
        // given
        List<InterestType> interestTypes = List.of(InterestType.REACT, InterestType.SPRING);

        Question q1 = questionRepository.save(
            Question.of("React 질문", QuestionType.REACT, "리액트 정답"));
        Question q2 = questionRepository.save(
            Question.of("Spring 질문", QuestionType.SPRING, "스프링 정답"));
        Question q3 = questionRepository.save(
            Question.of("DB 질문", QuestionType.DATABASE, "DB 정답"));
        Question q4 = questionRepository.save(
            Question.of("Dev 질문", QuestionType.DEVELOPMENT, "개발 공통 정답"));
        Question q5 = questionRepository.save(
            Question.of("React 질문", QuestionType.REACT, "리액트 정답"));
        Question q6 = questionRepository.save(
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
        List<QuestionResponse> result =
            questionService.getFilteredQuestions(MEMBER_ID, interestTypes);

        // then
        assertThat(result.size()).isLessThanOrEqualTo(3);

        List<Long> resultIds = result.stream().map(QuestionResponse::id).toList();
        assertThat(resultIds).isSubsetOf(q2.getId(), q4.getId(), q5.getId(), q6.getId());
        assertThat(resultIds).doesNotContain(q1.getId(), q3.getId());
    }

    @Test
    @Transactional
    @DisplayName("회원 관심사에 따른 질문 조회 시, 조건을 만족하는 질문이 3개 미만이면 해당 개수만큼 반환해야 한다.")
    void getFilteredQuestions_returnsLessThan3IfAvailableQuestionsAreLess() {
        // given
        List<InterestType> interestTypes = List.of(InterestType.REACT);

        Question q1 = questionRepository.save(
            Question.of("React 질문", QuestionType.REACT, "리액트 정답"));
        Question q2 = questionRepository.save(
            Question.of("React 질문", QuestionType.REACT, "리액트 정답"));
        Question q3 = questionRepository.save(
            Question.of("React 질문", QuestionType.REACT, "리액트 정답"));
        Question q4 = questionRepository.save(
            Question.of("React 질문", QuestionType.REACT, "리액트 정답"));
        Question q5 = questionRepository.save(
            Question.of("React 질문", QuestionType.REACT, "리액트 정답"));

        answerRepository.save(
            Answer.builder()
                .questionId(q1.getId())
                .memberId(MEMBER_ID)
                .interviewId(interviewId)
                .userAnswer("리액트 답변")
                .isUnderstood(true) // 이해함
                .status(AnswerStatus.CORRECT)
                .build()
        );

        answerRepository.save(
            Answer.builder()
                .questionId(q2.getId())
                .memberId(MEMBER_ID)
                .interviewId(interviewId)
                .userAnswer("리액트 답변")
                .isUnderstood(true) // 이해함
                .status(AnswerStatus.CORRECT)
                .build()
        );

        answerRepository.save(
            Answer.builder()
                .questionId(q3.getId())
                .memberId(MEMBER_ID)
                .interviewId(interviewId)
                .userAnswer("리액트 답변")
                .isUnderstood(true) // 이해함
                .status(AnswerStatus.CORRECT)
                .build()
        );

        // when
        List<QuestionResponse> result =
            questionService.getFilteredQuestions(MEMBER_ID, interestTypes);

        // then
        assertThat(result).hasSize(2); // q4, q5만 나와야 함

        List<Long> resultIds = result.stream().map(QuestionResponse::id).toList();
        assertThat(resultIds).contains(q4.getId(), q5.getId());
        assertThat(resultIds).doesNotContain(q1.getId(), q2.getId(), q3.getId());
    }
}
