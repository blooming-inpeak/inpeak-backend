package com.blooming.inpeak.answer.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnswerStatus {

    CORRECT(10), // 정답
    INCORRECT(5), // 오답
    SKIPPED(0), // 포기

    ALL(0); // 전체

    private final int expPoints;
}
