package com.blooming.inpeak.answer.service;

import com.blooming.inpeak.answer.domain.AnswerTask;
import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;
import com.blooming.inpeak.answer.dto.command.AnswerTaskMessage;
import com.blooming.inpeak.answer.dto.response.TaskIDResponse;
import com.blooming.inpeak.answer.repository.AnswerTaskRepository;
import com.blooming.inpeak.common.error.exception.NotFoundException;
import com.blooming.inpeak.question.domain.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerAsyncService {
    private final  AnswerManagerService answerManagerService;
    private final AnswerTaskRepository answerTaskRepository;
    private final KafkaTemplate<String, AnswerTaskMessage> kafkaTemplate;

    private static final String ANSWER_TASK_TOPIC = "answer-task-topic";

    /**
     * 비동기 답변 생성 요청 메서드
     *
     * @param command 답변 생성 명령어
     * @return 생성된 답변 작업 ID
     */
    @Transactional
    public TaskIDResponse requestAsyncAnswerCreation(AnswerCreateCommand command) {
        // 질문 유효성 검사 및 조회
        Question question = answerManagerService.validateAndGetQuestion(command);

        // 작업 큐 생성
        AnswerTask newTask = AnswerTask.waiting(command, question.getContent());

        // 작업 저장
        AnswerTask savedTask = answerTaskRepository.save(newTask);

        // 비동기 작업 요청
        kafkaTemplate.send(ANSWER_TASK_TOPIC, new AnswerTaskMessage(savedTask.getId()));

        return new TaskIDResponse( savedTask.getId());
    }

    /**
     * 답변 작업 재시도 메서드
     *
     * @param taskId   작업 ID
     * @param memberId 사용자 ID
     */
    @Transactional
    public void retryAnswerTask(Long taskId, Long memberId) {
        AnswerTask task = answerTaskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("AnswerTask 없음. taskId=" + taskId));

        // 작업 상태를 대기 상태로 변경
        task.retry();
        answerTaskRepository.save(task);

        // 비동기 작업 요청
        kafkaTemplate.send(ANSWER_TASK_TOPIC, new AnswerTaskMessage(task.getId()));
    }
}
