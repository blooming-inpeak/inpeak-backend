package com.blooming.inpeak.answer.domain;

import com.blooming.inpeak.member.domain.MemberStatistics;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnswerStatus {

    CORRECT(10) {
        @Override
        public void applyTo(MemberStatistics stats) {
            stats.increaseCorrect();
        }
    },
    INCORRECT(5) {
        @Override
        public void applyTo(MemberStatistics stats) {
            stats.increaseIncorrect();
        }
    },
    SKIPPED(0) {
        @Override
        public void applyTo(MemberStatistics stats) {
            stats.increaseSkipped();
        }
    },
    ALL(0) {
        @Override
        public void applyTo(MemberStatistics stats) {
        }
    };

    private final int expPoints;

    public abstract void applyTo(MemberStatistics stats);
}
