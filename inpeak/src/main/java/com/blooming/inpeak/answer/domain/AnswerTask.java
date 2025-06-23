package com.blooming.inpeak.answer.domain;

import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;
import com.blooming.inpeak.common.base.BaseEntity;
import com.blooming.inpeak.common.error.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "answer_tasks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerTask extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Long answerId;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private String questionContent;

    @Column(nullable = false)
    private Long interviewId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String audioFileUrl;

    @Column(nullable = true)
    private String videoUrl;

    @Column(nullable = false)
    private Long time; // 답변 시간 (초 또는 ms)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnswerTaskStatus status;

    @Builder
    public AnswerTask(Long id, Long answerId, Long questionId, Long interviewId,
        Long memberId, String audioFileUrl, String videoUrl, Long time,
        AnswerTaskStatus status, String questionContent) {
        this.id = id;
        this.answerId = answerId;
        this.questionId = questionId;
        this.interviewId = interviewId;
        this.memberId = memberId;
        this.questionContent = questionContent;
        this.audioFileUrl = removeQueryParams(audioFileUrl);
        this.videoUrl = removeQueryParams(videoUrl);
        this.time = time;
        this.status = status;
    }

    public static AnswerTask waiting(AnswerCreateCommand command, String questionContent) {
        return AnswerTask.builder()
            .questionId(command.questionId())
            .interviewId(command.interviewId())
            .memberId(command.memberId())
            .questionContent(questionContent)
            .audioFileUrl(command.audioURL())
            .videoUrl(command.videoURL())
            .time(command.time())
            .status(AnswerTaskStatus.WAITING)
            .build();
    }

    public void markSuccess(Long answerId) {
        this.status = AnswerTaskStatus.SUCCESS;
        this.answerId = answerId;
    }

    private static String removeQueryParams(String url) {
        if (url == null) return null;
        return url.split("\\?")[0];
    }

    public void markFailed() {
        this.status = AnswerTaskStatus.FAILED;
    }

    public void retry() {
        if (this.status != AnswerTaskStatus.FAILED) {
            throw new BadRequestException("작업 상태가 실패 상태가 아닙니다. 현재 상태: " + this.status);
        }

        this.status = AnswerTaskStatus.WAITING;
    }
}
