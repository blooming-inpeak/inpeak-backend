package com.blooming.inpeak.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        Integer status,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<FieldError> errors
) {
    public record FieldError(String field, String value, String reason) { }
}
