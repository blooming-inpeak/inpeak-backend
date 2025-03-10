package com.blooming.inpeak.answer.dto.request;

import jakarta.validation.constraints.NotNull;

public record CommentUpdateRequest(
    @NotNull String comment,
    @NotNull Long answerId
) { }
