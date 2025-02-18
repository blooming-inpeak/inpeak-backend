package com.blooming.inpeak.answer.dto.request;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.member.domain.Member;
import jakarta.validation.constraints.*;

public record CorrectAnswerFilterRequest(
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
            .status(AnswerStatus.CORRECT)
            .sortType(sortType)
            .page(page)
            .build();
    }
}
