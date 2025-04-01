package com.blooming.inpeak.auth.controller;

import com.blooming.inpeak.auth.dto.TokenResponse;
import com.blooming.inpeak.auth.service.AuthService;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
        return memberPrincipal.toString();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @AuthenticationPrincipal MemberPrincipal principal,
        HttpServletResponse response
    ) {
        authService.logout(principal.id(), response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissueToken(
        @AuthenticationPrincipal MemberPrincipal principal,
        HttpServletResponse response
    ) {
        // 이미 필터에서 인증되었으므로 principal.id()를 바로 사용
        TokenResponse tokenResponse = authService.reissueToken(principal.id(), response);
        return ResponseEntity.ok(tokenResponse);
    }

    // 필터를 통과하지 않은 경우를 위한 별도 엔드포인트 (하위 호환성)
    @PostMapping("/reissue/token")
    public ResponseEntity<TokenResponse> reissueTokenByRefreshToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        TokenResponse tokenResponse = authService.reissueTokenByRefreshToken(request, response);
        return ResponseEntity.ok(tokenResponse);
    }
}
