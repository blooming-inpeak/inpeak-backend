package com.blooming.inpeak.question.domain;

import com.blooming.inpeak.common.base.BaseEntity;
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
@Table(name = "questions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    @Column(nullable = false)
    private String bestAnswer;

    @Builder
    private Question(String content, QuestionType questionType, String bestAnswer) {
        this.content = content;
        this.questionType = questionType;
        this.bestAnswer = bestAnswer;
    }

    public static Question of(String content, QuestionType questionType, String bestAnswer) {
        return Question.builder()
            .content(content)
            .questionType(questionType)
            .bestAnswer(bestAnswer)
            .build();
    }
}
