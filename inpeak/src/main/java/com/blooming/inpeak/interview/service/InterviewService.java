package com.blooming.inpeak.interview.service;

import com.blooming.inpeak.interview.dto.response.InterviewTotalCountResponse;
import com.blooming.inpeak.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;

    public InterviewTotalCountResponse getTotalInterviewCount(Long memberId) {
        return InterviewTotalCountResponse.of(interviewRepository.countByMemberId(memberId));
    }
}
