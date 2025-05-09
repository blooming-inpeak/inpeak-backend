package com.blooming.inpeak.interview.service;

import com.blooming.inpeak.common.error.exception.BadRequestException;
import com.blooming.inpeak.common.error.exception.NotFoundException;
import com.blooming.inpeak.interview.dto.response.InterviewStartResponse;
import com.blooming.inpeak.member.domain.InterestType;
import com.blooming.inpeak.member.service.MemberInterestService;
import com.blooming.inpeak.question.dto.response.QuestionResponse;
import com.blooming.inpeak.question.service.QuestionService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewStartService {
    private final InterviewService interviewService;
    private final MemberInterestService memberInterestService;
    private final QuestionService questionService;

    @Transactional
    public InterviewStartResponse startInterview(Long memberId, LocalDate startDate) {

        // 남은 모의면접 횟수 확인
        int interviewCount = interviewService.getRemainingInterviews(memberId, startDate).count();
        if (interviewCount == 0) {
            throw new BadRequestException("모의면접 횟수가 모두 소진되었습니다.");
        }

        // 인터뷰 생성
        Long interviewId = interviewService.createInterview(memberId, startDate);

        // 사용자의 관심사를 가져와 질문을 필터링
        List<InterestType> interestTypes = memberInterestService.getMemberInterestTypes(memberId);
        if (interestTypes.isEmpty()) {
            throw new NotFoundException("관심사가 선택되지 않았습니다.");
        }

        List<QuestionResponse> questionResponse = questionService.getFilteredQuestions(memberId, interestTypes);

        return InterviewStartResponse.of(interviewId, questionResponse);
    }
}
