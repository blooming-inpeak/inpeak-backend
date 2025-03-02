package com.blooming.inpeak.answer.dto.request;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CorrectAnswerFilterRequest(
    @NotNull
    String sortType,
    @NotNull
    boolean isUnderstood,
    @Min(0)
    @NotNull
    int page
) {
    public AnswerFilterCommand toCommand(MemberPrincipal member, int size) {
        return AnswerFilterCommand.builder()
            .memberId(member.id())
            .isUnderstood(isUnderstood)
            .status(AnswerStatus.CORRECT)
            .sortType(sortType)
            .page(page)
            .size(size)
            .build();
    }
}
