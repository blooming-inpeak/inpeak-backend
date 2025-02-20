package com.blooming.inpeak.answer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.answer.dto.response.AnswerListResponse;
import com.blooming.inpeak.answer.dto.response.AnswersByInterviewResponse;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.answer.repository.AnswerRepositoryCustom;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private AnswerRepositoryCustom answerRepositoryCustom;

    @InjectMocks
    private AnswerService answerService;

    private Long memberId;
    private Pageable pageable;
    private AnswerFilterCommand command;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        pageable = PageRequest.of(0, 5);
        command = new AnswerFilterCommand(memberId, "DESC", true, AnswerStatus.CORRECT, 0, 5);
    }

    private Answer createAnswer(Long memberId, Long interviewId, String userAnswer,
        Long runningTime, AnswerStatus status) {
        return Answer.builder()
            .memberId(memberId)
            .interviewId(interviewId)
            .userAnswer(userAnswer)
            .videoURL("")
            .runningTime(runningTime)
            .comment("")
            .isUnderstood(false)
            .status(status)
            .build();
    }


    @DisplayName("getAnswerList()는 findAnswers()를 올바른 인자로 호출해야 한다.")
    @Test
    void getAnswerList_ShouldCallFindAnswersWithCorrectParams() {
        // given
        Slice<Answer> mockSlice = new SliceImpl<>(List.of(), pageable, false);
        when(answerRepositoryCustom.findAnswers(
            eq(command.memberId()), eq(command.isUnderstood()), eq(command.status()), eq(command.sortType()),
            any(Pageable.class)) // ✅ 모든 인자를 matcher로 통일
        ).thenReturn(mockSlice);

        // when
        answerService.getAnswerList(command);

        // then
        verify(answerRepositoryCustom, times(1))
            .findAnswers(eq(command.memberId()), eq(command.isUnderstood()), eq(command.status()),
                eq(command.sortType()), any(Pageable.class)); // ✅ matcher 적용
    }

    @DisplayName("getAnswerList()는 조회 결과가 없을 경우 빈 리스트를 반환해야 한다.")
    @Test
    void getAnswerList_ShouldReturnEmptyList_WhenNoResults() {
        // given
        Slice<Answer> emptySlice = new SliceImpl<>(List.of(), pageable, false);
        when(answerRepositoryCustom.findAnswers(
            eq(command.memberId()), eq(command.isUnderstood()), eq(command.status()), eq(command.sortType()),
            any(Pageable.class)) // ✅ matcher 적용
        ).thenReturn(emptySlice);

        // when
        AnswerListResponse response = answerService.getAnswerList(command);

        // then
        assertNotNull(response);
        assertTrue(response.AnswerResponseList().isEmpty());
        assertFalse(response.hasNext());
    }

    @DisplayName("skipAnswer()는 사용자의 답변을 스킵 상태로 저장해야 한다.")
    @Test
    void skipAnswer_ShouldSaveSkippedAnswer() {
        // given
        Long questionId = 100L;
        Long interviewId = 200L;

        // when
        answerService.skipAnswer(memberId, questionId, interviewId);

        // then
        verify(answerRepository, times(1)).save(any(Answer.class));
    }

    @DisplayName("getAnswersByDate()는 특정 날짜에 진행된 인터뷰별 답변 ID 목록을 반환해야 한다.")
    @Test
    void getAnswersByDate_ShouldReturnAnswersGroupedByInterview() {
        // given
        LocalDate date = LocalDate.of(2025, 2, 15);
        List<Answer> mockAnswers = List.of(
            createAnswer(memberId, 101L, "답변1", 120L, AnswerStatus.CORRECT),
            createAnswer(memberId, 101L, "답변2", 130L, AnswerStatus.INCORRECT),
            createAnswer(memberId, 102L, "답변3", 140L, AnswerStatus.SKIPPED),
            createAnswer(memberId, 102L, "답변4", 150L, AnswerStatus.CORRECT)
        );

        when(answerRepository.findAnswersByMemberAndDate(eq(memberId), eq(date)))
            .thenReturn(mockAnswers);

        // when
        List<AnswersByInterviewResponse> response = answerService.getAnswersByDate(memberId, date);

        // then
        assertNotNull(response);
        assertEquals(2, response.size(), "인터뷰 그룹 개수가 예상과 일치해야 합니다.");

        // 🔹 1️⃣ 인터뷰 ID별로 정렬된 상태인지 확인
        List<Long> expectedInterviewIds = List.of(101L, 102L);
        List<Long> actualInterviewIds = response.stream().map(AnswersByInterviewResponse::interviewId).toList();
        assertEquals(expectedInterviewIds, actualInterviewIds, "인터뷰 ID가 예상된 순서대로 정렬되어 있어야 합니다.");

        // 🔹 2️⃣ 각 인터뷰별 포함된 답변 개수 검증
        response.forEach(r -> assertEquals(2, r.answerIds().size(), "각 인터뷰 그룹은 정확히 2개의 답변을 포함해야 합니다."));

        // 🔹 3️⃣ Repository가 올바르게 호출되었는지 검증
        verify(answerRepository, times(1)).findAnswersByMemberAndDate(memberId, date);
    }
}
