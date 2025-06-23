package com.blooming.inpeak.answer.service;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerTask;
import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;
import com.blooming.inpeak.answer.dto.command.AnswerTaskMessage;
import com.blooming.inpeak.answer.repository.AnswerTaskRepository;
import com.blooming.inpeak.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerKafkaConsumer {

    private final AnswerTaskRepository answerTaskRepository;
    private final AnswerManagerService answerManagerService;
    private final GPTService gptService;
    private final AnswerPresignedUrlService answerPresignedUrlService;

    /**
     * Kafka 메시지를 수신하여 답변 작업을 처리하는 메서드
     *
     * @param message AnswerTaskMessage 객체
     */
    @KafkaListener(
        topics = "answer-task-topic",
        groupId = "answer-task-group"
    )
    public void listen(AnswerTaskMessage message) {
        Long taskId = message.taskId();

        AnswerTask task = answerTaskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("AnswerTask 없음. taskId=" + taskId));

        // 커맨드 재구성
        AnswerCreateCommand command = AnswerCreateCommand.from(task);

        // 음성 파일 다운 로드
        byte[] audioBytes = answerPresignedUrlService.downloadAudioFromS3(command.audioURL());

        try {
            // GPT 피드백 생성
            String feedback = gptService.makeGPTResponse(audioBytes, task.getQuestionContent());

            // 답변 생성
            Answer answer = answerManagerService.generateAnswer(command, feedback);

            // 성공 처리
            task.markSuccess(answer.getId());
            log.info("답변 생성 성공: taskId={}, answerId={}", taskId, answer.getId());
        } catch (Exception e) {
            // 실패 처리
            task.markFailed();
            log.error("답변 생성 실패: taskId={}, error={}", taskId, e.getMessage());
        } finally {
            answerTaskRepository.save(task);
        }
    }
}

