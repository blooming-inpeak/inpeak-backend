package com.blooming.inpeak.common.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 400 Bad Request
    INPUT_VALUE_INVALID("INPUT_VALUE_INVALID", "데이터 형식이 맞지 않습니다.", 400),
    BAD_REQUEST("BAD_REQUEST", "잘못된 요청입니다.", 400),

    // 401 Unauthorized
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", 401),

    // 403 Forbidden
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다.", 403),

    // 404 Not Found
    NOT_FOUND("NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.", 404),

    // 409 Conflict
    CONFLICT("CONFLICT", "요청이 서버 상태와 충돌했습니다.", 409),

    // 500 Internal Server Error
    ENCODING_FAILED("ENCODING_FAILED", "인코딩에 실패했습니다.", 500);

    private final String code;
    private final String message;
    private final Integer status;

    ErrorCode(String code, String message, Integer status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
