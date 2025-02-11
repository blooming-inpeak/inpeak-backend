package com.blooming.inpeak.common.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 400 Bad Request
    INPUT_VALUE_INVALID("INPUT_VALUE_INVALID", "데이터 형식이 맞지 않습니다.", 400),

    // 401 Unauthorized

    // 403 Forbidden

    // 404 Not Found

    // 409 Conflict
    ;

    private final String code;
    private final String message;
    private final Integer status;

    ErrorCode(String code, String message, Integer status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
