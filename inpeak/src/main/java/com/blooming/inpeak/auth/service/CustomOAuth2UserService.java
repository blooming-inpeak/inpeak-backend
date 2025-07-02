package com.blooming.inpeak.auth.service;

import com.blooming.inpeak.common.utils.NicknameGenerator;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.domain.MemberStatistics;
import com.blooming.inpeak.member.domain.OAuth2Provider;
import com.blooming.inpeak.member.domain.RegistrationStatus;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.repository.MemberRepository;
import com.blooming.inpeak.member.repository.MemberStatisticsRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final NicknameGenerator nicknameGenerator;
    private final MemberStatisticsRepository memberStatisticsRepository;

    // 테스트를 위해 DefaultOAuth2UserService를 필드로 추가
    private final DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 부모 클래스 메서드 호출 대신 필드 사용
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception e) {
            throw new OAuth2AuthenticationException("소셜 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        Long kakaoId = (Long) oAuth2User.getAttributes().get("id");

        @SuppressWarnings("unchecked")
        var kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
        String kakaoEmail = kakaoAccount.get("email").toString();

        Member member = memberRepository.findByKakaoId(kakaoId)
            .orElseGet(() -> registerNewMember(kakaoId, kakaoEmail));

        return MemberPrincipal.create(member, oAuth2User.getAttributes());
    }

    private Member registerNewMember(Long kakaoId, String kakaoEmail) {
        String uniqueNickname = nicknameGenerator.generateUniqueNickname();

        Member member = Member.builder()
            .kakaoId(kakaoId)
            .nickname(uniqueNickname)
            .provider(OAuth2Provider.KAKAO)
            .registrationStatus(RegistrationStatus.INITIATED)
            .kakaoEmail(kakaoEmail)
            .build();

        Member savedMember = memberRepository.save(member);
        memberStatisticsRepository.save(MemberStatistics.of(savedMember.getId()));

        return savedMember;
    }
}
