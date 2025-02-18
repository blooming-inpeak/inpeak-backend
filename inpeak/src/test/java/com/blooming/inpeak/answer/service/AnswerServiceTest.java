package com.blooming.inpeak.answer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.answer.dto.response.AnswerListResponse;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.answer.repository.AnswerRepositoryCustom;
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
        command = new AnswerFilterCommand(memberId, "DESC", true, AnswerStatus.CORRECT, 0);
    }

    @DisplayName("getAnswerList()는 findAnswers()를 올바른 인자로 호출해야 한다.")
    @Test
    void getAnswerList_ShouldCallFindAnswersWithCorrectParams() {
        // given
        Slice<Answer> mockSlice = new SliceImpl<>(List.of(), pageable, false);
        when(answerRepositoryCustom.findAnswers(
            command.memberId(), command.isUnderstood(), command.status(), command.sortType(), pageable)
        ).thenReturn(mockSlice);

        // when
        answerService.getAnswerList(command);

        // then
        verify(answerRepositoryCustom, times(1))
            .findAnswers(command.memberId(), command.isUnderstood(), command.status(), command.sortType(), pageable);
    }

    @DisplayName("getAnswerList()는 조회 결과가 없을 경우 빈 리스트를 반환해야 한다.")
    @Test
    void getAnswerList_ShouldReturnEmptyList_WhenNoResults() {
        // given
        Slice<Answer> emptySlice = new SliceImpl<>(List.of(), pageable, false);
        when(answerRepositoryCustom.findAnswers(
            command.memberId(), command.isUnderstood(), command.status(), command.sortType(), pageable)
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
        Answer skippedAnswer = Answer.ofSkipped(memberId, questionId, interviewId);

        // when
        answerService.skipAnswer(memberId, questionId, interviewId);

        // then
        verify(answerRepository, times(1)).save(any(Answer.class));
    }
}
