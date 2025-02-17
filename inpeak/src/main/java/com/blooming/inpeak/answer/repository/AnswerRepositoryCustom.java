package com.blooming.inpeak.answer.repository;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.domain.QAnswer;
import com.blooming.inpeak.interview.domain.QInterview;
import com.blooming.inpeak.question.domain.QQuestion;
import com.blooming.inpeak.member.domain.Member;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AnswerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 정답 답변 리스트를 동적으로 조회하는 메서드
     *
     * @param memberId 사용자 Id
     * @param isUnderstood 이해 여부
     * @param sortType 정렬 조건
     * @param pageable 페이징 정보
     * @return question, interview까지 페치 조인하여 전체 답변 리스트를 반환
     */
    public Slice<Answer> findCorrectAnswerList (
        Long memberId,
        boolean isUnderstood,
        String sortType,
        Pageable pageable
    ) {
        QAnswer answer = QAnswer.answer;
        QQuestion question = QQuestion.question;
        QInterview interview = QInterview.interview;

        // isUnderstood가 true일 경우에만 필터 적용
        BooleanExpression understoodFilter = isUnderstood
            ? answer.isUnderstood.eq(true) : null;

        // member 조건 추가
        BooleanExpression memberFilter = answer.memberId.eq(memberId);

        // AnswerStatus가 CORRECT인 조건 추가
        BooleanExpression statusFilter = answer.status.eq(AnswerStatus.CORRECT);

        //동적 정렬 조건 적용
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(sortType, answer);

        List<Answer> results = queryFactory
            .selectFrom(answer)
            .leftJoin(question).on(answer.questionId.eq(question.id)).fetchJoin()
            .leftJoin(interview).on(answer.interviewId.eq(interview.id)).fetchJoin()
            .where(understoodFilter, memberFilter, statusFilter)
            .orderBy(orderSpecifier)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        // 추가로 가져온 항목이 있으면 다음 페이지가 있음
        boolean hasNext = results.size() > pageable.getPageSize();
        if (hasNext) {
            results.remove(results.size() - 1); // 추가로 가져온 항목 제거
        }

        return new SliceImpl<>(results, pageable, hasNext);
    }

    private OrderSpecifier<?> getOrderSpecifier(String sortType, QAnswer answer) {
        if ("ASC".equalsIgnoreCase(sortType)) {
            return answer.createdAt.asc();
        }
        else if ("DESC".equalsIgnoreCase(sortType)) {
            return answer.createdAt.desc();
        }
        else {
            throw new IllegalArgumentException("올바르지 않은 정렬 타입입니다: " + sortType);
        }
    }

}
