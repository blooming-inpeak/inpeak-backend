package com.blooming.inpeak.interview.controller;

import com.blooming.inpeak.interview.dto.response.CalendarResponse;
import com.blooming.inpeak.interview.dto.response.InterviewStartResponse;
import com.blooming.inpeak.interview.service.InterviewService;
import com.blooming.inpeak.interview.service.InterviewStartService;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/interview")
public class InterviewController {

    private final InterviewService interviewService;
    private final InterviewStartService interviewStartService;

    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarResponse>> getCalendar(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @RequestParam int month,
        @RequestParam int year
    ) {
        return ResponseEntity.ok(interviewService.getCalendar(memberPrincipal.id(), month, year));
    }

    @PostMapping("/start")
    public ResponseEntity<InterviewStartResponse> startInterview(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @RequestParam LocalDate startDate
    ) {
        InterviewStartResponse response =
            interviewStartService.startInterview(memberPrincipal.id(), startDate);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
