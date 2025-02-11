package com.blooming.inpeak.answer.controller;

import com.blooming.inpeak.answer.controller.dto.request.AnswerSkipRequest;
import com.blooming.inpeak.answer.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/answer")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;

    @PostMapping("/skip")
    private ResponseEntity<Void> skipAnswer(
        @AuthenticationPrincipal Member member,
        @RequestBody AnswerSkipRequest request
    ) {
        answerService.skipAnswer(member, request.questionId(), request.interviewId());

    }
}
