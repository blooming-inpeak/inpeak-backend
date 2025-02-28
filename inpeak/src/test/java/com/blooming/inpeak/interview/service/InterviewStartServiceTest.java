package com.blooming.inpeak.interview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.interview.dto.response.InterviewStartResponse;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import com.blooming.inpeak.member.domain.InterestType;
import com.blooming.inpeak.member.domain.MemberInterest;
import com.blooming.inpeak.member.repository.MemberInterestRepository;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.domain.QuestionType;
import com.blooming.inpeak.question.dto.response.QuestionResponse;
import com.blooming.inpeak.question.repository.QuestionRepository;
import com.blooming.inpeak.support.IntegrationTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class InterviewStartServiceTest extends IntegrationTestSupport {

    @Autowired
    private InterviewStartService interviewStartService;

    @Autowired
    private MemberInterestRepository memberInterestRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    private static final Long MEMBER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.now();

    @Test
    @Transactional
    @DisplayName("남은 모의면접 횟수가 1 이상일 때, 인터뷰 시점을 기록하고 질문 3개를 반환해야 한다.")
    void startInterview_Success() {
        // given
        memberInterestRepository.save(MemberInterest.of(MEMBER_ID, InterestType.REACT));
        memberInterestRepository.save(MemberInterest.of(MEMBER_ID, InterestType.SPRING));

        Question q1 = questionRepository.save(
            Question.of("React 질문", QuestionType.REACT, "리액트 정답"));
        Question q2 = questionRepository.save(
            Question.of("Spring 질문", QuestionType.SPRING, "스프링 정답"));
        Question q3 = questionRepository.save(
            Question.of("Dev 질문", QuestionType.DEVELOPMENT, "공통 정답"));

        // when
        InterviewStartResponse response = interviewStartService.startInterview(MEMBER_ID, TODAY);

        // then
        assertThat(response.interviewId()).isNotNull();

        List<QuestionResponse> questions = response.questions();
        assertThat(questions.size()).isLessThanOrEqualTo(3);

        List<Long> questionIds = questions.stream().map(QuestionResponse::id).toList();
        assertThat(questionIds).contains(q1.getId(), q2.getId(), q3.getId());
    }

    @Test
    @DisplayName("남은 모의면접 횟수가 0이면 예외가 발생해야 한다.")
    void startInterview_Fail_WhenNoRemainingInterviews() {
        // given
        interviewRepository.save(Interview.of(MEMBER_ID, TODAY));

        // when & then
        assertThatThrownBy(() ->
            interviewStartService.startInterview(MEMBER_ID, TODAY)
        ).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("남은 모의면접 횟수가 없습니다.");
    }
}
