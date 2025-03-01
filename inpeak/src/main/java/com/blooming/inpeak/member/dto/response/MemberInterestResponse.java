package com.blooming.inpeak.member.dto.response;

import java.util.List;

public record MemberInterestResponse(List<String> interests) {

    public static MemberInterestResponse of(List<String> interests) {
        return new MemberInterestResponse(interests);
    }
}
