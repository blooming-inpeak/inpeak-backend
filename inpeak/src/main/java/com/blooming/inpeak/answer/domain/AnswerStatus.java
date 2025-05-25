package com.blooming.inpeak.answer.domain;

import com.blooming.inpeak.member.domain.MemberStatistics;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnswerStatus {

    CORRECT(10, MemberStatistics::increaseCorrect),
    INCORRECT(5, MemberStatistics::increaseIncorrect),
    SKIPPED(0, MemberStatistics::increaseSkipped),
    ALL(0, stats -> {});

    private final int expPoints;
    private final Consumer<MemberStatistics> action;

    public void applyTo(MemberStatistics stats) {
        action.accept(stats);
    }
}
