package com.blooming.inpeak.member.controller;

import com.blooming.inpeak.member.domain.Member;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.dto.request.NincknameUpdateRequest;
import com.blooming.inpeak.member.dto.response.MyPageResponse;
import com.blooming.inpeak.member.service.MemberInterestService;
import com.blooming.inpeak.member.service.MemberService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberInterestService memberInterestService;

    @PatchMapping
    public ResponseEntity<String> updateNickName(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @Valid @RequestBody NincknameUpdateRequest request
    ) {
        String updateNickName = memberService.updateNickName(memberPrincipal.id(), request.nickname());
        return ResponseEntity.ok(updateNickName);
    }

    @DeleteMapping
    public ResponseEntity<Void> withdrawMember(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal
    ) {
        memberService.withdrawMember(memberPrincipal.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<MyPageResponse> getMyInfo(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal
    ) {
        Member member = memberService.getMemberInfo(memberPrincipal.id());
        List<String> interests = memberInterestService.getMemberInterestStrings(
            memberPrincipal.id()).interests();

        return ResponseEntity.ok(MyPageResponse.from(member, interests));
    }
}
