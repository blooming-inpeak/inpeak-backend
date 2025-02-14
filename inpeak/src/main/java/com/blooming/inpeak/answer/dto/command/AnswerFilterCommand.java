package com.blooming.inpeak.answer.dto.command;

import com.blooming.inpeak.member.domain.Member;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AnswerFilterCommand(
    Member member,

    String sortType,

    boolean isUnderstood,

    int page
) { }
