package com.blooming.inpeak.answer.dto.command;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.member.domain.Member;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AnswerFilterCommand(
    Long memberId,

    String sortType,

    boolean isUnderstood,

    AnswerStatus status,

    int page,

    int size
) { }
