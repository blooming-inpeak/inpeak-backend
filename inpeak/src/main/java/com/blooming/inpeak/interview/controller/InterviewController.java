package com.blooming.inpeak.interview.controller;

import com.blooming.inpeak.interview.dto.response.CalendarResponse;
import com.blooming.inpeak.interview.service.InterviewService;
import com.blooming.inpeak.member.domain.Member;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InterviewController {
    private final InterviewService interviewService;
    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarResponse>> getCalendar(
        @AuthenticationPrincipal Member member,
        @RequestParam int month,
        @RequestParam int year
    ) {
        return ResponseEntity.ok(interviewService.getCalendar(member.getId(), month, year));
    }
}
