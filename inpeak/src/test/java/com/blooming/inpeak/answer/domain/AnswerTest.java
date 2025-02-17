package com.blooming.inpeak.answer.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.blooming.inpeak.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnswerTest {

    @DisplayName("스킵된 답변 생성 테스트")
    @Test
    void ofSkipped() {
        //given
        Long memberId = 1L;
        Long questionId = 100L;
        Long interviewId = 200L;

        //when
        Answer skippedAnswer = Answer.ofSkipped(memberId, questionId, interviewId);

        //then
        assertNotNull(skippedAnswer);
        assertEquals(questionId, skippedAnswer.getQuestionId());
        assertEquals(memberId, skippedAnswer.getMemberId());
        assertEquals(interviewId, skippedAnswer.getInterviewId());
        assertEquals(AnswerStatus.SKIPPED, skippedAnswer.getStatus()); // 스킵된 상태 설정
    }
}
