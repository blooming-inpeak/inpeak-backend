package com.blooming.inpeak.member.controller;

import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.dto.response.MemberInterestResponse;
import com.blooming.inpeak.member.service.MemberInterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interest")
@RequiredArgsConstructor
public class MemberInterestController {

    private final MemberInterestService memberInterestService;

    @GetMapping("/list")
    public ResponseEntity<MemberInterestResponse> getMemberInterest(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal
    ) {
        return ResponseEntity.ok(
            memberInterestService.getMemberInterestStrings(memberPrincipal.id()));
    }
}
