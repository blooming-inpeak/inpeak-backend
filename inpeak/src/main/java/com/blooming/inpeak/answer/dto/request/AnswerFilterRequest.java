package com.blooming.inpeak.answer.dto.request;

import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.member.domain.Member;
import jakarta.validation.constraints.*;

public record AnswerFilterRequest(
    @NotNull
    String sortType,
    @NotNull
    boolean isUnderstood,
    @Min(0)
    @NotNull
    int page
) {
    public AnswerFilterCommand toCommand(Member member) {
        return AnswerFilterCommand.builder()
            .memberId(member.getId())
            .isUnderstood(isUnderstood)
            .sortType(sortType)
            .page(page)
            .build();
    }
}
