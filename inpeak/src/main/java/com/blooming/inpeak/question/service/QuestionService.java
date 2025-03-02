package com.blooming.inpeak.question.service;

import com.blooming.inpeak.member.domain.InterestType;
import com.blooming.inpeak.question.domain.Question;
import com.blooming.inpeak.question.domain.QuestionType;
import com.blooming.inpeak.question.dto.response.QuestionResponse;
import com.blooming.inpeak.question.repository.QuestionRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;

    private static final int QUESTION_COUNT = 3;

    /**
     * 회원의 관심사에 따른 질문 리스트를 가져오는 메서드
     *
     * @param memberId 회원 ID
     * @param interestTypes 회원의 관심사
     * @return 질문 리스트
     */
    public List<QuestionResponse> getFilteredQuestions(Long memberId, List<InterestType> interestTypes) {
        // InterestType -> QuestionType 변환 후 직군별 기본 질문 타입 추가
        List<QuestionType> types = convertToQuestionType(interestTypes);
        List<String> typeStrings = addDefaultQuestionType(types);

        // 회원의 관심사에 따른 질문 리스트 조회
        List<Question> questions = questionRepository.findFilteredQuestionsByTypes(typeStrings, memberId);

        // 랜덤으로 3개만 선택하여 반환
        Collections.shuffle(questions);
        return questions.stream().limit(QUESTION_COUNT)
            .map(QuestionResponse::from)
            .toList();
    }

    /**
     * InterestType을 QuestionType으로 변환하는 메서드
     *
     * @param interestTypes InterestType 리스트
     * @return QuestionType 리스트
     */
    private List<QuestionType> convertToQuestionType(List<InterestType> interestTypes) {
        return interestTypes.stream()
            .map(interestType -> QuestionType.valueOf(interestType.name()))
            .collect(Collectors.toList());
    }

    /**
     * 직군별 기본 질문 타입을 추가하는 메서드
     *
     * @param types 질문 타입 리스트
     * @return 기본 질문 타입이 추가된 리스트
     */
    private List<String> addDefaultQuestionType(List<QuestionType> types) {
        types.add(QuestionType.DEVELOPMENT);

        return types.stream()
            .map(Enum::name)
            .toList();
    }
}
