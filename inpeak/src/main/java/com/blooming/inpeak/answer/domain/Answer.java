package com.blooming.inpeak.answer.domain;

import com.blooming.inpeak.common.base.BaseEntity;
import com.blooming.inpeak.interview.domain.Interview;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer extends BaseEntity {
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

    // 저장 시에는 사용되지 않고 값을 조회할 때만 사용되는 필드들 페치 조인을 위해 사용된다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionId", insertable = false, updatable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewId", insertable = false, updatable = false)
    private Interview interview;


    private String userAnswer;

    private String videoURL;

    private Long runningTime;

    private String comment;

    private boolean isUnderstood;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AnswerStatus status;

    @Builder
    private Answer(Long questionId, Long memberId, Long interviewId, String userAnswer, String videoURL,
        Long runningTime, String comment, Boolean isUnderstood, AnswerStatus status) {
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
    public static Answer ofSkipped(Long memberId, Long questionId, Long interviewId) {
        return Answer.builder()
            .questionId(questionId)
            .memberId(memberId)
            .interviewId(interviewId)
            .status(AnswerStatus.SKIPPED) // 스킵된 상태 설정
            .isUnderstood(false) // 이해하지 못한 상태로 설정
            .build();
    }
}
