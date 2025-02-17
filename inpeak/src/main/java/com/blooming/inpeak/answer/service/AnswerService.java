package com.blooming.inpeak.answer.service;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.answer.dto.response.AnswerListResponse;
import com.blooming.inpeak.answer.dto.response.AnswerResponse;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.answer.repository.AnswerRepositoryCustom;
import com.blooming.inpeak.member.domain.Member;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final AnswerRepositoryCustom answerRepositoryCustom;

    /**
     * 답변을 스킵하는 메서드
     *
     * @param member 사용자 정보
     * @param questionId 답변 ID
     * @param interviewId 인터뷰 ID
     */
    public void skipAnswer(Member member, Long questionId, Long interviewId) {
        Answer skippedAnswer = Answer.ofSkipped(member.getId(), questionId, interviewId);
        answerRepository.save(skippedAnswer);
    }

    /**
     * 사용자 답변 중 정답인 데이터만 반환하는 메서드
     *
     * @param command 사용자 정보와 검색 조건을 담은 command
     * @return 정답인 답변을 담은 리스트와 다음 페이지가 있는 지 여부를 반환
     */
    public AnswerListResponse getCorrectAnswerList(AnswerFilterCommand command) {
        Pageable pageable = PageRequest.of(command.page(), 5);

        Slice<Answer> results = answerRepositoryCustom
            .findCorrectAnswerList(
                command.member(),
                command.isUnderstood(),
                command.sortType(),
                pageable
            );

        List<AnswerResponse> answerResponses = results.getContent().stream()
            .map(AnswerResponse::from)
            .toList();

        return new AnswerListResponse(answerResponses, results.hasNext());
    }
}
