package com.blooming.inpeak.answer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.dto.command.AnswerFilterCommand;
import com.blooming.inpeak.answer.dto.response.AnswerListResponse;
import com.blooming.inpeak.answer.repository.AnswerRepository;
import com.blooming.inpeak.answer.repository.AnswerRepositoryCustom;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import com.blooming.inpeak.answer.domain.Answer;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private AnswerRepositoryCustom answerRepositoryCustom;

    @InjectMocks
    private AnswerService answerService;

    private Long memberId;
    private Long questionId;
    private Long interviewId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        memberId = 1L; // Member 객체 생성
        questionId = 1L;
        interviewId = 1L;
        pageable = PageRequest.of(0, 5);
    }

    @DisplayName("skipAnswer()는 사용자의 답변을 스킵 상태로 저장해야 한다.")
    @Test
    void testSkipAnswer() {
        // given
        ArgumentCaptor<Answer> answerCaptor = ArgumentCaptor.forClass(Answer.class);

        // when
        answerService.skipAnswer(memberId, questionId, interviewId);

        // then
        verify(answerRepository, times(1)).save(answerCaptor.capture());

        Answer capturedAnswer = answerCaptor.getValue(); // 저장된 객체 가져오기
        assertEquals(memberId, capturedAnswer.getMemberId());
        assertEquals(questionId, capturedAnswer.getQuestionId());
        assertEquals(interviewId, capturedAnswer.getInterviewId());
        assertEquals(AnswerStatus.SKIPPED, capturedAnswer.getStatus());
    }

    @DisplayName("getCorrectAnswerList()는 정답 목록을 조회하기 위해 레포지토리를 호출해야 한다.")
    @Test
    void getCorrectAnswerList_ShouldCallFetchCorrectAnswerSlice() {
        // given
        Slice<Answer> mockSlice = new SliceImpl<>(List.of(), pageable, false);
        when(answerRepositoryCustom.findCorrectAnswerList(any(), anyBoolean(), anyString(), any()))
            .thenReturn(mockSlice);

        AnswerFilterCommand command = new AnswerFilterCommand(memberId, "DESC", true, 0);

        // when
        answerService.getCorrectAnswerList(command);

        // then
        verify(answerRepositoryCustom, times(1))
            .findCorrectAnswerList(command.memberId(), command.isUnderstood(), command.sortType(), pageable);
    }

    @DisplayName("getCorrectAnswerList()는 조회 결과가 없을 경우 빈 리스트를 반환해야 한다.")
    @Test
    void getCorrectAnswerList_ShouldHandleEmptyResult() {
        // given
        Slice<Answer> emptySlice = new SliceImpl<>(Collections.emptyList(), pageable, false);

        when(answerRepositoryCustom.findCorrectAnswerList(any(), anyBoolean(), anyString(), any()))
            .thenReturn(emptySlice);

        AnswerFilterCommand command = new AnswerFilterCommand(memberId, "DESC", true, 0);

        // when
        AnswerListResponse response = answerService.getCorrectAnswerList(command);

        // then
        assertNotNull(response);
        assertTrue(response.AnswerResponseList().isEmpty()); // 빈 리스트 확인
        assertFalse(response.hasNext()); // hasNext = false 확인
    }
}

