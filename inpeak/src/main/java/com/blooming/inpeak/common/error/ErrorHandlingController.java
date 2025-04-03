package com.blooming.inpeak.common.error;

import static com.blooming.inpeak.common.error.ErrorBuildFactory.*;

import com.blooming.inpeak.common.error.exception.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class ErrorHandlingController {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("잘못된 요청 값이 전달되었습니다.");
        List<ErrorResponse.FieldError> fieldErrors = getFieldErrors(e.getBindingResult());
        return buildFieldErrors(ErrorCode.INPUT_VALUE_INVALID, fieldErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ErrorResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("타입이 맞지 않는 요청 값입니다.");
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(e.getPropertyName(), (String) e.getValue(), e.getMessage());
        return buildFieldErrors(ErrorCode.INPUT_VALUE_INVALID, List.of(fieldError));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ErrorResponse handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("올바르지 않은 요청 값이 전달되었습니다. " + e.getMessage());
        return buildError(ErrorCode.INPUT_VALUE_INVALID);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ErrorResponse handleBadRequestException(BadRequestException e) {
        log.error("잘못된 요청: {}", e.getMessage());
        return buildError(ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected ErrorResponse handleUnauthorizedException(UnauthorizedException e) {
        log.error("인증 실패: {}", e.getMessage());
        return buildError(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    protected ErrorResponse handleForbiddenException(ForbiddenException e) {
        log.error("접근 거부: {}", e.getMessage());
        return buildError(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ErrorResponse handleNotFoundException(NotFoundException e) {
        log.error("리소스를 찾을 수 없음: {}", e.getMessage());
        return buildError(ErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    protected ErrorResponse handleConflictException(ConflictException e) {
        log.error("요청 충돌: {}", e.getMessage());
        return buildError(ErrorCode.CONFLICT);
    }
}
