package com.blooming.inpeak.answer.service;

import com.blooming.inpeak.answer.domain.AnswerTask;
import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;
import com.blooming.inpeak.answer.dto.command.AnswerTaskMessage;
import com.blooming.inpeak.answer.repository.AnswerTaskRepository;
import com.blooming.inpeak.common.error.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AnswerKafkaConsumerTest {

    private AnswerTaskRepository answerTaskRepository;
    private AnswerManagerService answerManagerService;
    private GPTService gptService;
    private AnswerPresignedUrlService answerPresignedUrlService;
    private AnswerKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        answerTaskRepository = mock(AnswerTaskRepository.class);
        answerManagerService = mock(AnswerManagerService.class);
        gptService = mock(GPTService.class);
        answerPresignedUrlService = mock(AnswerPresignedUrlService.class);

        consumer = new AnswerKafkaConsumer(
            answerTaskRepository,
            answerManagerService,
            gptService,
            answerPresignedUrlService
        );
    }

    @Test
    void listen_성공_테스트() {
        // given
        AnswerTask task = Mockito.spy(
            AnswerTask.waiting(
                new AnswerCreateCommand(
                    "audio-url",   // audioURL
                    10L,           // time
                    3L,            // memberId
                    4L,            // questionId
                    5L,            // interviewId
                    "video-url"    // videoURL
                ),
                "질문 내용"
            )
        );
        when(answerTaskRepository.findById(1L)).thenReturn(java.util.Optional.of(task));
        when(answerPresignedUrlService.downloadAudioFromS3(any())).thenReturn(new byte[]{1, 2, 3});
        when(gptService.makeGPTResponse(any(), any())).thenReturn("유저답변@CORRECT@AI 피드백");
        when(answerManagerService.generateAnswer(any(), any())).thenReturn(
            mock(com.blooming.inpeak.answer.domain.Answer.class)
        );
        when(answerManagerService.generateAnswer(any(), any()).getId()).thenReturn(100L);

        // when
        consumer.listen(new AnswerTaskMessage(1L));

        // then
        verify(task).markSuccess(100L);
        verify(answerTaskRepository).save(task);
    }

    @Test
    void listen_실패_테스트() {
        // given
        AnswerTask task = Mockito.spy(
            AnswerTask.waiting(
                new AnswerCreateCommand(
                    "audio-url", 10L, 3L, 4L, 5L, "video-url"
                ),
                "질문 내용"
            )
        );
        when(answerTaskRepository.findById(1L)).thenReturn(java.util.Optional.of(task));
        when(answerPresignedUrlService.downloadAudioFromS3(any())).thenReturn(new byte[]{9, 9, 9});
        when(gptService.makeGPTResponse(any(), any())).thenThrow(new RuntimeException("GPT 오류"));

        // when
        consumer.listen(new AnswerTaskMessage(1L));

        // then
        verify(task).markFailed();
        verify(answerTaskRepository).save(task);
    }

    @Test
    void listen_존재하지않는_taskId() {
        // given
        when(answerTaskRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThrows(NotFoundException.class,
            () -> consumer.listen(new AnswerTaskMessage(99L)));
    }
}
