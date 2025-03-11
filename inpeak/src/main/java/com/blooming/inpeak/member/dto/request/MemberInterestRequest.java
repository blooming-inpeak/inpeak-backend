package com.blooming.inpeak.member.dto.request;

import com.blooming.inpeak.member.domain.InterestType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record MemberInterestRequest(
    @NotNull(message = "관심사를 입력해주세요.")
    @NotEmpty(message = "최소 하나 이상의 관심 분야를 선택해야 합니다.")
    List<InterestType> interestTypes
) {
}
