package com.blooming.inpeak.dashborad.service;

import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.dashborad.dto.SuccessRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuccessRateService {

    private final AnswerRepository answerRepository;

    public SuccessRateResponse getSuccessRate(Long memberId) {
        return SuccessRateResponse.of(
            answerRepository.getMemberSuccessRate(memberId),
            answerRepository.getAverageSuccessRate()
        );
    }
}
