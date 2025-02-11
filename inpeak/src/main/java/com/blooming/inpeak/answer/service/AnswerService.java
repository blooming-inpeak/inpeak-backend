package com.blooming.inpeak.answer.service;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;

    /**
     * 답변을 스킵하는 메서드
     *
     * @param member 사용자 정보
     * @param questionId 답변 ID
     * @param interviewId 인터뷰 ID
     */
    public void skipAnswer(Member member, Long questionId, Long interviewId) {
        Answer skippedAnswer = Answer.ofSkipped(member, questionId, interviewId);
        answerRepository.save(skippedAnswer);
    }
}
