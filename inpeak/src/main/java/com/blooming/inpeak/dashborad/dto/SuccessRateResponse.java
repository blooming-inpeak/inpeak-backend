package com.blooming.inpeak.dashborad.dto;

import lombok.Builder;

@Builder
public record SuccessRateResponse(
    int userSuccessRate,
    int averageSuccessRate
) {
    public static SuccessRateResponse of(int userSuccessRate, int averageSuccessRate) {
        return SuccessRateResponse.builder()
            .userSuccessRate(userSuccessRate)
            .averageSuccessRate(averageSuccessRate)
            .build();
    }
}
