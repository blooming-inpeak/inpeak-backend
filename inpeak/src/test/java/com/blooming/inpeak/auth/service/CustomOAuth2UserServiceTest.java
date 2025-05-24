package com.blooming.inpeak.auth.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.blooming.inpeak.common.utils.NicknameGenerator;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.domain.OAuth2Provider;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.repository.MemberRepository;
import com.blooming.inpeak.member.repository.MemberStatisticsRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("CustomOAuth2UserService 테스트")
@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NicknameGenerator nicknameGenerator;

    @Mock
    private DefaultOAuth2UserService defaultOAuth2UserService;

    @Mock
    private MemberStatisticsRepository memberStatisticsRepository;

    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        // CustomOAuth2UserService 생성 및 defaultOAuth2UserService 주입
        customOAuth2UserService = new CustomOAuth2UserService(
            memberRepository,
            nicknameGenerator,
            memberStatisticsRepository
        );

        // ReflectionTestUtils를 사용하여 부모 클래스의 모킹된 서비스를 주입
        ReflectionTestUtils.setField(customOAuth2UserService, "defaultOAuth2UserService", defaultOAuth2UserService);
    }

    @Test
    @DisplayName("기존 회원 로드 성공")
    void loadExistingUser() {
        // given
        String testEmail = "test@test.com";
        Long kakaoId = 12345678L;
        Member existingMember = Member.builder()
            .kakaoEmail(testEmail)
            .kakaoId(kakaoId)
            .nickname("기존회원")
            .provider(OAuth2Provider.KAKAO)
            .totalQuestionCount(0L)
            .correctAnswerCount(0L)
            .build();

        // 테스트 데이터 설정
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", testEmail);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", kakaoId);
        attributes.put("kakao_account", kakaoAccount);

        OAuth2User oAuth2User = new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "id"
        );

        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("kakao")
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost:8080/login/oauth2/code/kakao")
            .scope("account_email")
            .authorizationUri("https://kauth.kakao.com/oauth/authorize")
            .tokenUri("https://kauth.kakao.com/oauth/token")
            .userInfoUri("https://kapi.kakao.com/v2/user/me")
            .userNameAttributeName("id")
            .clientName("Kakao")
            .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "test-token-value",
            Instant.now(),
            Instant.now().plus(Duration.ofHours(1))
        );

        OAuth2UserRequest userRequest = new OAuth2UserRequest(
            clientRegistration,
            accessToken
        );

        // Mocking - DefaultOAuth2UserService가 항상 우리가 설정한 OAuth2User를 반환하도록 설정
        when(defaultOAuth2UserService.loadUser(any(OAuth2UserRequest.class))).thenReturn(oAuth2User);

        // Mocking - 기존 회원이 존재하는 경우
        when(memberRepository.findByKakaoId(kakaoId)).thenReturn(Optional.of(existingMember));

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result).isInstanceOf(MemberPrincipal.class);
    }

    @Test
    @DisplayName("신규 회원 등록 성공")
    void registerNewUser() {
        // given
        Long kakaoId = 12345678L;
        String generatedNickname = "신규닉네임123";
        String tokenValue = "new-token-value";
        String testEmail = "test@test.com";

        Member newMember = Member.builder()
            .kakaoId(kakaoId)
            .nickname(generatedNickname)
            .provider(OAuth2Provider.KAKAO)
            .totalQuestionCount(0L)
            .correctAnswerCount(0L)
            .kakaoEmail(testEmail)
            .build();

        // 테스트 데이터 설정
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", testEmail);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", kakaoId);
        attributes.put("kakao_account", kakaoAccount);

        OAuth2User oAuth2User = new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "id"
        );

        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("kakao")
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost:8080/login/oauth2/code/kakao")
            .scope("account_email")
            .authorizationUri("https://kauth.kakao.com/oauth/authorize")
            .tokenUri("https://kauth.kakao.com/oauth/token")
            .userInfoUri("https://kapi.kakao.com/v2/user/me")
            .userNameAttributeName("id")
            .clientName("Kakao")
            .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            tokenValue,
            Instant.now(),
            Instant.now().plus(Duration.ofHours(1))
        );

        OAuth2UserRequest userRequest = new OAuth2UserRequest(
            clientRegistration,
            accessToken
        );

        // Mocking
        when(defaultOAuth2UserService.loadUser(any(OAuth2UserRequest.class))).thenReturn(oAuth2User);
        when(memberRepository.findByKakaoId(kakaoId)).thenReturn(Optional.empty());
        when(nicknameGenerator.generateUniqueNickname()).thenReturn(generatedNickname);
        when(memberRepository.save(any(Member.class))).thenReturn(newMember);

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result).isInstanceOf(MemberPrincipal.class);
        MemberPrincipal principal = (MemberPrincipal) result;
        assertThat(principal.kakaoId()).isEqualTo(kakaoId);
    }
}
