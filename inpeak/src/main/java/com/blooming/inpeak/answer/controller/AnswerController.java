package com.blooming.inpeak.answer.controller;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.answer.dto.request.AnswerSkipRequest;
import com.blooming.inpeak.answer.dto.request.CommentUpdateRequest;
import com.blooming.inpeak.answer.dto.request.CorrectAnswerFilterRequest;
import com.blooming.inpeak.answer.dto.request.IncorrectAnswerFilterRequest;
import com.blooming.inpeak.answer.dto.request.UnderstoodUpdateRequest;
import com.blooming.inpeak.answer.dto.response.AnswerDetailResponse;
import com.blooming.inpeak.answer.dto.response.AnswerIDResponse;
import com.blooming.inpeak.answer.dto.response.AnswerListResponse;
import com.blooming.inpeak.answer.dto.response.AnswerPresignedUrlResponse;
import com.blooming.inpeak.answer.dto.response.InterviewWithAnswersResponse;
import com.blooming.inpeak.answer.dto.response.RecentAnswerListResponse;
import com.blooming.inpeak.answer.service.AnswerPresignedUrlService;
import com.blooming.inpeak.answer.service.AnswerService;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/answer")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;
    private final AnswerPresignedUrlService answerPresignedUrlService;

    @PostMapping("/skip")
    public ResponseEntity<AnswerIDResponse> skipAnswer(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @RequestBody AnswerSkipRequest request
    ) {
        AnswerIDResponse response = answerService.skipAnswer(memberPrincipal.id(),
            request.questionId(), request.interviewId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/correct")
    public ResponseEntity<AnswerListResponse> getCorrectAnswerList(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        CorrectAnswerFilterRequest request
    ) {
        AnswerFilterCommand command = request.toCommand(memberPrincipal, 5);
        return ResponseEntity.ok(answerService.getAnswerList(command));
    }

    @GetMapping("/incorrect")
    public ResponseEntity<AnswerListResponse> getIncorrectAnswerList(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        IncorrectAnswerFilterRequest request
    ) {
        AnswerFilterCommand command = request.toCommand(memberPrincipal, 10);
        return ResponseEntity.ok(answerService.getAnswerList(command));
    }

    @GetMapping("/date")
    public ResponseEntity<InterviewWithAnswersResponse> getAnswersByDate(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(answerService.getAnswersByDate(memberPrincipal.id(), date));
    }

    @GetMapping("/recent")
    public ResponseEntity<RecentAnswerListResponse> getRecentAnswers(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @RequestParam(required = false, defaultValue = "ALL") AnswerStatus status
    ) {
        return ResponseEntity.ok(answerService.getRecentAnswers(memberPrincipal.id(), status));
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<AnswerPresignedUrlResponse> getPresignedUrl(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @RequestParam LocalDate startDate,
        @RequestParam String extension
    ) {
        return ResponseEntity.ok(
            answerPresignedUrlService.getPreSignedUrl(memberPrincipal.id(), startDate, extension));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnswerIDResponse> createAnswer(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @RequestParam("audioFile") MultipartFile audioFile,
        @RequestParam("time") Long time,
        @RequestParam("questionId") Long questionId,
        @RequestParam("interviewId") Long interviewId,
        @RequestParam(value = "videoURL", required = false) String videoURL
    ) {
        AnswerIDResponse response = answerService.createAnswer(
            AnswerCreateCommand.of(audioFile, time, memberPrincipal.id(),
                questionId, interviewId, videoURL));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/understood")
    public ResponseEntity<Void> updateUnderstood(
        @RequestBody UnderstoodUpdateRequest request,
        @AuthenticationPrincipal MemberPrincipal memberPrincipal
    ) {
        answerService.updateUnderstood(request.answerId(), request.isUnderstood(), memberPrincipal.id());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/comment")
    public ResponseEntity<Void> updateComment(
        @RequestBody CommentUpdateRequest request
    ) {
        answerService.updateComment(request.answerId(), request.comment());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<AnswerDetailResponse> getAnswer(
        @RequestParam Long interviewId,
        @RequestParam Long questionId,
        @AuthenticationPrincipal MemberPrincipal memberPrincipal
    ) {
        AnswerDetailResponse response = answerService.getAnswer(interviewId, questionId,
            memberPrincipal.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{answerId}")
    public ResponseEntity<AnswerDetailResponse> getAnswerById(
        @PathVariable Long answerId,
        @AuthenticationPrincipal MemberPrincipal memberPrincipal
    ) {
        AnswerDetailResponse response = answerService.getAnswerById(answerId, memberPrincipal.id());
        return ResponseEntity.ok(response);
    }
}
