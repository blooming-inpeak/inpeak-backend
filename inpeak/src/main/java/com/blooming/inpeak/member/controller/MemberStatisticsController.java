package com.blooming.inpeak.member.controller;

import com.blooming.inpeak.member.dto.MemberPrincipal;
import com.blooming.inpeak.member.dto.response.MemberStatsResponse;
import com.blooming.inpeak.member.service.MemberStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberStatisticsController {

    private final MemberStatisticsService memberStatisticsService;

    @GetMapping("/answer/summary")
    public ResponseEntity<MemberStatsResponse> getSummary(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal
    ) {
        MemberStatsResponse response = memberStatisticsService.getMemberStats(memberPrincipal.id());
        return ResponseEntity.ok(response);
    }
}
