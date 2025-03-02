package com.blooming.inpeak.answer.dto.request;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.member.dto.MemberPrincipal;
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
    public AnswerFilterCommand toCommand(MemberPrincipal member, int size) {
        return AnswerFilterCommand.builder()
            .memberId(member.id())
            .status(status)
            .sortType(sortType)
            .page(page)
            .size(size)
            .build();
    }
}
