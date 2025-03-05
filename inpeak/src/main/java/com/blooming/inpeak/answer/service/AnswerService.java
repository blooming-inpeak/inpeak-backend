package com.blooming.inpeak.answer.service;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.answer.dto.response.AnswerListResponse;
import com.blooming.inpeak.answer.dto.response.AnswerResponse;
import com.blooming.inpeak.answer.dto.response.InterviewWithAnswersResponse;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.answer.repository.AnswerRepositoryCustom;
import com.blooming.inpeak.interview.domain.Interview;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final AnswerRepositoryCustom answerRepositoryCustom;

    /**
     * 답변을 스킵하는 메서드
     *
     * @param memberId    사용자 ID
     * @param questionId  답변 ID
     * @param interviewId 인터뷰 ID
     */
    @Transactional
    public void skipAnswer(Long memberId, Long questionId, Long interviewId) {
        Answer skippedAnswer = Answer.ofSkipped(memberId, questionId, interviewId);
        answerRepository.save(skippedAnswer);
    }

    /**
     * 답변을 불러오는 메서드 인자에 따라 다른 값을 불러온다.
     *
     * @param command 검색 조건
     * @return 답변들과 페이징 정보
     */
    public AnswerListResponse getAnswerList(AnswerFilterCommand command) {
        Pageable pageable = PageRequest.of(command.page(), command.size());

        // 공통된 로직: 답변 리스트 가져오기
        Slice<Answer> results = answerRepositoryCustom.findAnswers(
            command.memberId(),
            command.isUnderstood(),
            command.status(),
            command.sortType(),
            pageable
        );

        // 공통된 로직: DTO 변환
        List<AnswerResponse> answerResponses = results.getContent().stream()
            .map(AnswerResponse::from)
            .toList();

        return new AnswerListResponse(answerResponses, results.hasNext());
    }

    /**
     * 해당 날짜에 진행한 인터뷰에 대한 답변 리스트 반환
     *
     * @param memberId 사용자 ID
     * @param date     날짜
     * @return 인터뷰 ID, 답변 ID, 질문 제목 등
     */
    public InterviewWithAnswersResponse getAnswersByDate(Long memberId, LocalDate date) {
        List<Answer> answers = answerRepository.findAnswersByMemberAndDate(memberId, date);

        // ✅ 인터뷰가 한 개만 존재하므로 findFirst() 사용
        Interview interview = answers.stream()
            .map(Answer::getInterview)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("해당 날짜에 진행된 인터뷰가 없습니다."));

        return InterviewWithAnswersResponse.from(interview, answers);
    }

}
