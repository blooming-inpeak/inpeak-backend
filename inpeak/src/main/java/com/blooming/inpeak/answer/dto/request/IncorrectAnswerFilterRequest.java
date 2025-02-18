package com.blooming.inpeak.answer.dto.request;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.member.domain.Member;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record IncorrectAnswerFilterRequest(
    @NotNull
    String sortType,
    @NotNull
    AnswerStatus status,
    @Min(0)
    @NotNull
    int page
) {
    public AnswerFilterCommand toCommand(Member member) {
        return AnswerFilterCommand.builder()
            .memberId(member.getId())
            .status(status)
            .sortType(sortType)
            .page(page)
            .build();
    }
}
