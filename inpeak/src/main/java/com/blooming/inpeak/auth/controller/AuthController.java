package com.blooming.inpeak.auth.controller;

import com.blooming.inpeak.auth.service.AuthService;
import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/test")
    public String test(@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
        System.out.println("member = " + memberPrincipal.email());
        return memberPrincipal.toString();
    }

    // TODO: Authentication 객체를 사용하는 것이 좋을까요? @AuthenticationPrincipal 애너테이션을 사용하는 것이 좋을까요?
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();

        authService.logout(principal.id());
        return ResponseEntity.ok().build();
    }
}
