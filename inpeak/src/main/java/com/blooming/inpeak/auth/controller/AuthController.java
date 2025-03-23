package com.blooming.inpeak.auth.controller;

import com.blooming.inpeak.auth.dto.TokenResponse;
import com.blooming.inpeak.auth.service.AuthService;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/test")
    public String test(@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
        System.out.println("member = " + memberPrincipal.kakaoId());
        return memberPrincipal.toString();
    }

    // TODO: Authentication 객체를 사용하는 것이 좋을까요? @AuthenticationPrincipal 애너테이션을 사용하는 것이 좋을까요?
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();

        authService.logout(principal.id());
        return ResponseEntity.ok().build();
    }

    // 토큰 재발급 엔드포인트 추가
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authService.reissueToken(request);

        addTokenCookie(response, "accessToken", tokenResponse.accessToken(),
            (int)(tokenResponse.accessTokenExpiresIn() / 1000));
        addTokenCookie(response, "refreshToken", tokenResponse.refreshToken(),
            (int)(tokenResponse.refreshTokenExpiresIn() / 1000));

        return ResponseEntity.ok(tokenResponse);
    }

    private void addTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
//        cookie.setSecure(true); // HTTPS 환경에서만 활성화 // TODO: 어떻게 하는 것이 좋을까?
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
