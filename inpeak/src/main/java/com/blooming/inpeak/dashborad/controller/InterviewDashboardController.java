package com.blooming.inpeak.dashborad.controller;

import com.blooming.inpeak.dashborad.dto.InterviewDashboardResponse;
import com.blooming.inpeak.dashborad.service.InterviewDashboardService;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InterviewDashboardController {

    private final InterviewDashboardService interviewDashboardService;

    @GetMapping("/interview")
    public ResponseEntity<InterviewDashboardResponse> getDashboard(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @RequestParam LocalDate startDate
    ) {
        InterviewDashboardResponse response =
            interviewDashboardService.getDashboard(memberPrincipal.id(), startDate);
        return ResponseEntity.ok(response);
    }
}
