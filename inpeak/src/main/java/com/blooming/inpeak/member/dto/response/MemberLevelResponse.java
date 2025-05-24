package com.blooming.inpeak.member.dto.response;

import lombok.Builder;

@Builder
public record MemberLevelResponse(
    int level,
    int currentExp,
    int nextExp
) {
    public static MemberLevelResponse of(int level, int currentExp, int nextExp) {
        return MemberLevelResponse.builder()
            .level(level)
            .currentExp(currentExp)
            .nextExp(nextExp)
            .build();
    }
}
