package com.blooming.inpeak.answer.service;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.command.AnswerCreateCommand;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.answer.dto.response.AnswerListResponse;
import com.blooming.inpeak.answer.dto.response.AnswerResponse;
import com.blooming.inpeak.answer.dto.response.InterviewWithAnswersResponse;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.answer.repository.AnswerRepositoryCustom;
import com.blooming.inpeak.answer.repository.UserAnswerStatsRepository;
import com.blooming.inpeak.interview.domain.Interview;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.repository.QuestionRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final AnswerRepositoryCustom answerRepositoryCustom;
    private final GPTService gptService;
    private final QuestionRepository questionRepository;
    private final UserAnswerStatsRepository userAnswerStatsRepository;

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

    /**
     * 답변을 생성하는 메서드
     *
     * @param command 답변 생성 명령
     */
    public void createAnswer(AnswerCreateCommand command) {
        Question question = questionRepository.findById(command.questionId())
            .orElseThrow(() -> new IllegalArgumentException("해당 질문이 존재하지 않습니다."));

        String feedback = gptService.makeGPTResponse(command.audioFile(), question.getContent());

        Answer answer = Answer.of(command, feedback);
        answerRepository.save(answer);

        userAnswerStatsRepository.incrementUserAnswerStat(answer.getMemberId(), answer.getStatus());
    }

    /**
     * 답변을 이해했는지 여부를 업데이트하는 메서드
     *
     * @param answerId     답변 ID
     * @param isUnderstood 사용자가 이해했는지 여부
     */
    public void updateUnderstood(Long answerId, boolean isUnderstood) {
        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new IllegalArgumentException("해당 답변이 존재하지 않습니다."));

        answer.setUnderstood(isUnderstood);
        answerRepository.save(answer);
    }
}
