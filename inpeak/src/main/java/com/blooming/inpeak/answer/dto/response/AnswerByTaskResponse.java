package com.blooming.inpeak.answer.dto.response;

public record AnswerByTaskResponse(
    Long taskId,
    String status,
    Long answerId) {
    public static AnswerByTaskResponse waiting(Long taskId) {
        return new AnswerByTaskResponse(taskId, "WAITING", null);
    }

    public static AnswerByTaskResponse failed(Long taskId) {
        return new AnswerByTaskResponse(taskId, "FAILED", null);
    }

    public static AnswerByTaskResponse success(Long taskId, Long answerId) {
        return new AnswerByTaskResponse(taskId, "SUCCESS", answerId);
    }
}
