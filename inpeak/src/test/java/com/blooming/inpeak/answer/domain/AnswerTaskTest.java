package com.blooming.inpeak.answer.domain;

import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;
import com.blooming.inpeak.common.error.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AnswerTaskTest {

    @Test
    void waiting_정상_생성() {
        // given
        AnswerCreateCommand command = new AnswerCreateCommand("audioUrl", 30L, 3L, 1L, 2L, "videoUrl");
        String questionContent = "Spring과 Spring Boot의 차이";

        // when
        AnswerTask task = AnswerTask.waiting(command, questionContent);

        // then
        assertThat(task.getQuestionId()).isEqualTo(1L);
        assertThat(task.getInterviewId()).isEqualTo(2L);
        assertThat(task.getMemberId()).isEqualTo(3L);
        assertThat(task.getAudioFileUrl()).isEqualTo("audioUrl");
        assertThat(task.getVideoUrl()).isEqualTo("videoUrl");
        assertThat(task.getTime()).isEqualTo(30L);
        assertThat(task.getQuestionContent()).isEqualTo(questionContent);
        assertThat(task.getStatus()).isEqualTo(AnswerTaskStatus.WAITING);
    }

    @Test
    void markSuccess_정상작동() {
        AnswerTask task = createFailedTask();
        task.markSuccess(100L);

        assertThat(task.getStatus()).isEqualTo(AnswerTaskStatus.SUCCESS);
        assertThat(task.getAnswerId()).isEqualTo(100L);
    }

    @Test
    void markFailed_정상작동() {
        AnswerTask task = createWaitingTask();
        task.markFailed();

        assertThat(task.getStatus()).isEqualTo(AnswerTaskStatus.FAILED);
    }

    @Test
    void retry_성공_조건() {
        AnswerTask task = createFailedTask();
        task.retry();

        assertThat(task.getStatus()).isEqualTo(AnswerTaskStatus.WAITING);
    }

    @Test
    void retry_실패시_예외발생() {
        AnswerTask task = createWaitingTask();

        assertThatThrownBy(task::retry)
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("작업 상태가 실패 상태가 아닙니다");
    }

    private AnswerTask createFailedTask() {
        return AnswerTask.builder()
            .questionId(1L)
            .interviewId(1L)
            .memberId(1L)
            .questionContent("dummy")
            .audioFileUrl("audio.mp3")
            .videoUrl("video.mp4")
            .time(10L)
            .status(AnswerTaskStatus.FAILED)
            .build();
    }

    private AnswerTask createWaitingTask() {
        return AnswerTask.builder()
            .questionId(1L)
            .interviewId(1L)
            .memberId(1L)
            .questionContent("dummy")
            .audioFileUrl("audio.mp3")
            .videoUrl("video.mp4")
            .time(10L)
            .status(AnswerTaskStatus.WAITING)
            .build();
    }
}
