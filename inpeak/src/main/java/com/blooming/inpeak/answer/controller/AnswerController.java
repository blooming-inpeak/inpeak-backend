package com.blooming.inpeak.answer.controller;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.answer.dto.request.AnswerCreateRequest;
import com.blooming.inpeak.answer.dto.request.AnswerSkipRequest;
import com.blooming.inpeak.answer.dto.request.CommentUpdateRequest;
import com.blooming.inpeak.answer.dto.request.CorrectAnswerFilterRequest;
import com.blooming.inpeak.answer.dto.request.IncorrectAnswerFilterRequest;
import com.blooming.inpeak.answer.dto.request.UnderstoodUpdateRequest;
import com.blooming.inpeak.answer.dto.response.AnswerByTaskResponse;
import com.blooming.inpeak.answer.dto.response.AnswerDetailResponse;
import com.blooming.inpeak.answer.dto.response.AnswerIDResponse;
import com.blooming.inpeak.answer.dto.response.AnswerListResponse;
import com.blooming.inpeak.answer.dto.response.AnswerPresignedUrlResponse;
import com.blooming.inpeak.answer.dto.response.InterviewWithAnswersResponse;
import com.blooming.inpeak.answer.dto.response.RecentAnswerListResponse;
import com.blooming.inpeak.answer.dto.response.TaskIDResponse;
import com.blooming.inpeak.answer.service.AnswerAsyncService;
import com.blooming.inpeak.answer.service.AnswerService;
import com.blooming.inpeak.member.dto.MemberPrincipal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/answer")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;
    private final AnswerAsyncService answerAsyncService;

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
        @RequestParam(required = false) String extension,
        @RequestParam Boolean includeVideo
    ) {
        return ResponseEntity.ok(answerService.getPresignedUrl(
            memberPrincipal.id(), startDate, extension, includeVideo));
    }

    @PostMapping("/create")
    public ResponseEntity<TaskIDResponse> createAnswer(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @RequestBody AnswerCreateRequest request
    ) {
        return ResponseEntity.ok(answerAsyncService.requestAsyncAnswerCreation(request.toCommand(
            memberPrincipal.id())));
    }

    @PutMapping("/understood")
    public ResponseEntity<Void> updateUnderstood(
        @RequestBody UnderstoodUpdateRequest request,
        @AuthenticationPrincipal MemberPrincipal memberPrincipal
    ) {
        answerService.updateUnderstood(request.answerId(), request.isUnderstood(),
            memberPrincipal.id());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/comment")
    public ResponseEntity<Void> updateComment(
        @RequestBody CommentUpdateRequest request
    ) {
        answerService.updateComment(request.answerId(), request.comment());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<AnswerByTaskResponse> findAnswerByTaskId(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @PathVariable Long taskId) {
        return answerService.findAnswerByTaskId(taskId, memberPrincipal.id());
    }

    @GetMapping("/{answerId}")
    public ResponseEntity<AnswerDetailResponse> getAnswerById(
        @PathVariable Long answerId,
        @AuthenticationPrincipal MemberPrincipal memberPrincipal
    ) {
        AnswerDetailResponse response = answerService.getAnswerById(answerId, memberPrincipal.id());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tasks/{taskId}/retry")
    public ResponseEntity<Void> retryAnswerTask(
        @AuthenticationPrincipal MemberPrincipal memberPrincipal,
        @PathVariable Long taskId
    ) {
        answerAsyncService.retryAnswerTask(taskId, memberPrincipal.id());
        return ResponseEntity.ok().build();
    }
}
