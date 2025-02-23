package com.blooming.inpeak.question.service;

import com.blooming.inpeak.member.service.MemberInterestsService;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.domain.QuestionType;
import com.blooming.inpeak.question.dto.response.QuestionResponse;
import com.blooming.inpeak.question.repository.QuestionRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final MemberInterestsService memberInterestsService;

    private static final int QUESTION_COUNT = 3;

    public List<QuestionResponse> getFilteredQuestions(Long memberId) {

        List<QuestionType> types = memberInterestsService.getUserQuestionTypes(memberId);

        // QuestionType -> String 변환
        List<String> typeStrings = types.stream()
            .map(Enum::name)
            .collect(Collectors.toList());

        // 회원의 관심사에 따른 질문 리스트 조회
        List<Question> questions = questionRepository.findFilteredQuestionsByTypes(typeStrings, memberId);

        // 랜덤으로 3개만 선택하여 반환
        Collections.shuffle(questions);
        return questions.stream().limit(QUESTION_COUNT)
            .map(QuestionResponse::from)
            .toList();
    }
}
