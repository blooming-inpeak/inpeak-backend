package com.blooming.inpeak.member.controller;

import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.dto.request.NincknameUpdateRequest;
import com.blooming.inpeak.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PatchMapping
    public ResponseEntity<String> updateNickName(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @Valid @RequestBody NincknameUpdateRequest request
    ) {
        String updateNickName = memberService.updateNickName(memberPrincipal.id(), request.nickname());
        return ResponseEntity.ok(updateNickName);
    }
}
