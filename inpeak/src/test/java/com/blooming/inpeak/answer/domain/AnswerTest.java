package com.blooming.inpeak.answer.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnswerTest {

    @DisplayName("스킵된 답변 생성 테스트")
    @Test
    void ofSkipped() {
        // ✅ Given
        Long memberId = 1L;
        Long questionId = 100L;
        Long interviewId = 200L;

        // ✅ When
        Answer skippedAnswer = Answer.ofSkipped(memberId, questionId, interviewId);

        // ✅ Then
        assertNotNull(skippedAnswer);
        assertEquals(questionId, skippedAnswer.getQuestionId());
        assertEquals(memberId, skippedAnswer.getMemberId());
        assertEquals(interviewId, skippedAnswer.getInterviewId());
        assertEquals(AnswerStatus.SKIPPED, skippedAnswer.getStatus()); // 스킵된 상태 설정
    }

    @DisplayName("정상적인 답변 생성 및 문자열 트림 검증 테스트")
    @Test
    void answerOf_ShouldCreateAnswer_AndTrimCorrectly() {
        // ✅ Given (테스트 데이터 준비)
        AnswerCreateCommand command = new AnswerCreateCommand(
            "audioFile123", 10, 1L, 1L, 1L, "videoURL123"
        );

        // ✅ 문자열에 공백 포함 (trim 동작 확인)
        String feedback = "  User's answer   @  CORRECT  @  AI feedback message   ";

        // ✅ When (Answer 객체 생성)
        Answer answer = Answer.of(command, feedback);

        // ✅ Then (검증)
        assertNotNull(answer);

        // 🔹 splitAndTrimText() 결과가 정상적으로 반영되었는지 검증
        assertEquals("User's answer", answer.getUserAnswer());  // 앞뒤 공백 제거됨
        assertEquals(AnswerStatus.CORRECT, answer.getStatus()); // Enum 변환 검증
        assertEquals("AI feedback message", answer.getAIAnswer()); // 공백 제거됨

        // 🔹 기타 Answer 필드 검증
        assertEquals(command.questionId(), answer.getQuestionId());
        assertEquals(command.memberId(), answer.getMemberId());
        assertEquals(command.interviewId(), answer.getInterviewId());
        assertEquals(command.videoURL(), answer.getVideoURL());
        assertEquals(command.time(), answer.getRunningTime());
        assertFalse(answer.isUnderstood());
    }

    @DisplayName("정답 상태일 때 사용자가 이해 여부를 업데이트할 수 있다.")
    @Test
    void setUnderstood_ShouldUpdate_WhenStatusIsCorrect() {
        // given
        Answer answer = Answer.builder()
            .status(AnswerStatus.CORRECT) // 정답 상태로 설정
            .isUnderstood(false)
            .build();

        // when
        answer.setUnderstood(true);

        // then
        assertThat(answer.isUnderstood()).isTrue();
    }

    @DisplayName("정답이 아닌 상태에서 이해 여부를 업데이트하려고 하면 예외가 발생해야 한다.")
    @Test
    void setUnderstood_ShouldThrowException_WhenStatusIsNotCorrect() {
        // given
        Answer answer = Answer.builder()
            .status(AnswerStatus.INCORRECT) // 정답이 아닌 상태
            .isUnderstood(false)
            .build();

        // when, then
        assertThatThrownBy(() -> answer.setUnderstood(true))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("정답인 경우에만 이해 여부를 업데이트할 수 있습니다.");
    }
}
