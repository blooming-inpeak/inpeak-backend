package com.blooming.inpeak.member.service;

import com.blooming.inpeak.member.domain.Interests;
import com.blooming.inpeak.member.repository.MemberInterestsRepository;
import com.blooming.inpeak.question.domain.QuestionType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberInterestsService {

    private final MemberInterestsRepository memberInterestsRepository;

    /**
     * 회원의 관심사를 가져와 QuestionType으로 변환하는 메서드
     *
     * @param memberId 사용자 ID
     * @return QuestionType형의 회원 관심사
     */
    @Transactional
    public List<QuestionType> getUserQuestionTypes(Long memberId) {
        List<Interests> interests = memberInterestsRepository.findInterestsByMemberId(memberId);

        // Interests -> QuestionType 변환
        List<QuestionType> questionTypes = interests.stream()
            .map(interest -> QuestionType.valueOf(interest.name()))
            .collect(Collectors.toList());

        // 개발자용 공통 질문 `DEVELOPMENT` 추가
        questionTypes.add(QuestionType.DEVELOPMENT);

        return questionTypes;
    }
}
