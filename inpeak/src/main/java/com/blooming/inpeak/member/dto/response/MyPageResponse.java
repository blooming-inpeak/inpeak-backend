package com.blooming.inpeak.member.dto.response;

import com.blooming.inpeak.member.domain.Member;
import java.util.List;
import lombok.Builder;

@Builder
public record MyPageResponse(
    String nickname,
    String kakaoEmail,
    List<String> interests
) {
    public static MyPageResponse from(Member member, List<String> interests) {
        return MyPageResponse.builder()
            .nickname(member.getNickname())
            .kakaoEmail(member.getKakaoEmail())
            .interests(interests)
            .build();
    }
}
