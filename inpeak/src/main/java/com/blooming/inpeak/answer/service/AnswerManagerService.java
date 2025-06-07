package com.blooming.inpeak.answer.service;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.common.error.exception.ConflictException;
import com.blooming.inpeak.common.error.exception.NotFoundException;
import com.blooming.inpeak.member.service.MemberStatisticsService;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerManagerService {
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final MemberStatisticsService memberStatisticsService;

    @Transactional(readOnly = true)
    public Question validateAndGetQuestion(AnswerCreateCommand command) {
        if (answerRepository.existsByInterviewIdAndQuestionId(command.interviewId(),
            command.questionId())) {
            throw new ConflictException("이미 답변이 존재하는 질문입니다.");
        }

        return questionRepository.findById(command.questionId())
            .orElseThrow(() -> new NotFoundException("해당 질문이 존재하지 않습니다."));
    }

    @Transactional
    public Answer generateAnswer(AnswerCreateCommand command, String feedback) {
        Answer answer = Answer.of(command, feedback);
        answerRepository.save(answer);

        // 회원 통계 업데이트
        memberStatisticsService.updateStatistics(command.memberId(), answer.getStatus());

        return answer;
    }
}
