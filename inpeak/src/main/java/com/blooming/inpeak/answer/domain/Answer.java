package com.blooming.inpeak.answer.domain;

import com.blooming.inpeak.common.base.AuditingFields;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer extends AuditingFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    // 답변에서 member 객체가 필요한 경우는 없을 것 같아 id만 저장
    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long interviewId;

    private String userAnswer;

    private String videoURL;

    private Long runningTime;

    private String comment;

    @Column(nullable = false)
    private boolean isUnderstood;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AnswerStatus status;

    @Builder
    private Answer(Long questionId, Long memberId, Long interviewId, String userAnswer, String videoURL,
        Long runningTime, String comment, boolean isUnderstood, AnswerStatus status) {
        this.questionId = questionId;
        this.memberId = memberId;
        this.interviewId = interviewId;
        this.userAnswer = userAnswer;
        this.videoURL = videoURL;
        this.runningTime = runningTime;
        this.comment = comment;
        this.isUnderstood = isUnderstood;
        this.status = status;
    }

    // 스킵된 답변 생성
    public static Answer ofSkipped(Member member, Long questionId, Long interviewId) {
        return Answer.builder()
            .questionId(questionId)
            .memberId(member.getId())
            .interviewId(interviewId)
            .isUnderstood(false) // 스킵된 답변은 이해 여부 false
            .status(AnswerStatus.SKIPPED) // 스킵된 상태 설정
            .build();
    }
}
