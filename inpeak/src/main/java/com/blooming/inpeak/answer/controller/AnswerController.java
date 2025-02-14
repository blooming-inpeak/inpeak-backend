package com.blooming.inpeak.answer.controller;

import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.answer.dto.request.AnswerFilterRequest;
import com.blooming.inpeak.answer.dto.request.AnswerSkipRequest;
import com.blooming.inpeak.answer.dto.response.AnswerListResponse;
import com.blooming.inpeak.answer.service.AnswerService;
import com.blooming.inpeak.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/answer")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;

    @PostMapping("/skip")
    public ResponseEntity<Void> skipAnswer(
        @AuthenticationPrincipal Member member,
        @RequestBody AnswerSkipRequest request
    ) {
        answerService.skipAnswer(member, request.questionId(), request.interviewId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/correct")
    public ResponseEntity<AnswerListResponse> getCorrectAnswerList (
        @AuthenticationPrincipal Member member,
        AnswerFilterRequest request
    ) {
        AnswerFilterCommand command = request.toCommand(member);
        return ResponseEntity.ok(answerService.getCorrectAnswerList(command));
    }
}
