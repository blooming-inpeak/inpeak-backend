package com.blooming.inpeak.answer.domain;

import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;
import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand2;
import com.blooming.inpeak.common.base.BaseEntity;
import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.question.domain.Question;
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
import java.util.Arrays;
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

    @Column(name = "video_url")
    private String videoURL;

    private Long runningTime;

    private String comment;

    private boolean isUnderstood;

    private String AIAnswer;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AnswerStatus status;

    @Builder
    private Answer(Long questionId, Long memberId, Long interviewId, String userAnswer,
        String videoURL,
        Long runningTime, String comment, Boolean isUnderstood, AnswerStatus status,
        String AIAnswer) {
        this.questionId = questionId;
        this.memberId = memberId;
        this.interviewId = interviewId;
        this.userAnswer = userAnswer;
        this.videoURL = videoURL;
        this.runningTime = runningTime;
        this.comment = comment;
        this.isUnderstood = isUnderstood;
        this.status = status;
        this.AIAnswer = AIAnswer;
    }

    /**
     * 사용자가 답변을 스킵한 경우의 Answer 객체를 생성하는 메서드
     *
     * @param memberId 사용자 ID
     * @param questionId 질문 ID
     * @param interviewId 인터뷰 ID
     * @return Answer 객체
     */
    public static Answer ofSkipped(Long memberId, Long questionId, Long interviewId) {
        return Answer.builder()
            .questionId(questionId)
            .memberId(memberId)
            .interviewId(interviewId)
            .status(AnswerStatus.SKIPPED) // 스킵된 상태 설정
            .isUnderstood(false) // 이해하지 못한 상태로 설정
            .build();
    }

    /**
     * 사용자가 제출한 답변을 저장하는 메서드
     *
     * @param command 답변 생성 명령
     * @param feedback GPT로부터 받은 피드백
     * @return Answer 객체
     */
    public static Answer of(AnswerCreateCommand command, String feedback) {
        String[] texts = splitAndTrimText(feedback);

        String userAnswer = texts[0];
        AnswerStatus status = AnswerStatus.valueOf(texts[1]);
        String AIAnswer = texts[2];
        String trimmedVideoURL = removeQueryParams(command.videoURL());

        return Answer.builder()
            .questionId(command.questionId())
            .memberId(command.memberId())
            .interviewId(command.interviewId())
            .userAnswer(userAnswer)
            .videoURL(trimmedVideoURL)
            .runningTime(command.time())
            .isUnderstood(false)
            .status(status)
            .AIAnswer(AIAnswer)
            .build();
    }

    /**
     * 사용자가 제출한 답변을 저장하는 메서드
     *
     * @param command 답변 생성 명령
     * @param feedback GPT로부터 받은 피드백
     * @return Answer 객체
     */
    public static Answer of(AnswerCreateCommand2 command, String feedback) {
        String[] texts = splitAndTrimText(feedback);

        AnswerStatus status = AnswerStatus.valueOf(texts[0]);
        String AIAnswer = texts[1];
        String trimmedVideoURL = removeQueryParams(command.videoURL());

        return Answer.builder()
            .questionId(command.questionId())
            .memberId(command.memberId())
            .interviewId(command.interviewId())
            .userAnswer(command.userAnswer())
            .videoURL(trimmedVideoURL)
            .runningTime(command.time())
            .isUnderstood(false)
            .status(status)
            .AIAnswer(AIAnswer)
            .build();
    }

    private static String[] splitAndTrimText(String feedback) {
        return Arrays.stream(feedback.split("@"))
            .map(String::trim) // 각 문자열에 trim 적용
            .toArray(String[]::new);
    }

    private static String removeQueryParams(String url) {
        if (url == null) return null;
        return url.split("\\?")[0];
    }

    /**
     * 사용자가 답변을 이해했는지 여부를 업데이트하는 메서드
     *
     * @param isUnderstood 사용자가 이해했는지 여부
     */
    public void setUnderstood(boolean isUnderstood) {
        if (this.status != AnswerStatus.CORRECT) {
            throw new IllegalStateException("정답인 경우에만 이해 여부를 업데이트할 수 있습니다.");
        }

        this.isUnderstood = isUnderstood;
    }

    /**
     * 답변에 코멘트를 추가하는 메서드
     *
     * @param comment 코멘트
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
}
