package com.blooming.inpeak.answer.dto.request;

import jakarta.validation.constraints.NotNull;

public record UnderstoodUpdateRequest(
    @NotNull Long answerId,
    @NotNull boolean isUnderstood
) { }
