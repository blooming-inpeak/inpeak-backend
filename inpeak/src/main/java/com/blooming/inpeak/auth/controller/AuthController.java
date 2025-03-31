package com.blooming.inpeak.auth.controller;

import com.blooming.inpeak.auth.dto.TokenResponse;
import com.blooming.inpeak.auth.service.AuthService;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        TokenResponse tokenResponse = authService.reissueToken(request, response);

        return ResponseEntity.ok(tokenResponse);
    }
}
