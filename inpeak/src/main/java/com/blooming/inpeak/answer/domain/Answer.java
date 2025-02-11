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

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long interviewId;

    private String userAnswer;

    private String videoURL;

    private Long runningTime;

    private String comment;

    @Column(nullable = false)
    private boolean isUnderstood;

    @Enumerated(EnumType.STRING)
    private AnswerStatus status;
}
