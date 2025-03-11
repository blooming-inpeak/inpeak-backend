package com.blooming.inpeak.member.controller;

import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.dto.request.MemberInterestRequest;
import com.blooming.inpeak.member.dto.response.MemberInterestResponse;
import com.blooming.inpeak.member.service.MemberInterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping
    public ResponseEntity<Void> registerInterests(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @Valid @RequestBody MemberInterestRequest request
    ) {
        memberInterestService.registerInitialInterests(memberPrincipal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateInterests(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @Valid @RequestBody MemberInterestRequest request
    ) {
        memberInterestService.updateInterests(memberPrincipal.id(), request);
        return ResponseEntity.ok().build();
    }
}
