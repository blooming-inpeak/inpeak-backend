package com.blooming.inpeak.answer.controller;

import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.answer.dto.request.IncorrectAnswerFilterRequest;
import com.blooming.inpeak.answer.dto.request.CorrectAnswerFilterRequest;
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
        answerService.skipAnswer(member.getId(), request.questionId(), request.interviewId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/correct")
    public ResponseEntity<AnswerListResponse> getCorrectAnswerList (
        @AuthenticationPrincipal Member member,
        CorrectAnswerFilterRequest request
    ) {
        AnswerFilterCommand command = request.toCommand(member, 5);
        return ResponseEntity.ok(answerService.getAnswerList(command));
    }

    @GetMapping("/incorrect")
    public ResponseEntity<AnswerListResponse> getIncorrectAnswerList (
        @AuthenticationPrincipal Member member,
        IncorrectAnswerFilterRequest request
    ) {
        AnswerFilterCommand command = request.toCommand(member, 10);
        return ResponseEntity.ok(answerService.getAnswerList(command));
    }
}
