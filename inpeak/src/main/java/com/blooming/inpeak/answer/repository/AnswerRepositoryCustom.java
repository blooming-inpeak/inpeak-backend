package com.blooming.inpeak.answer.repository;

import com.blooming.inpeak.answer.domain.Answer;
import com.blooming.inpeak.answer.domain.AnswerStatus;
import com.blooming.inpeak.answer.domain.QAnswer;
import com.blooming.inpeak.interview.domain.QInterview;
import com.blooming.inpeak.question.domain.QQuestion;
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
     * 답변 리스트를 동적으로 조회하는 메서드
     *
     * @param memberId 사용자 Id
     * @param isUnderstood 이해 여부
     * @param status 답변 분류 지정
     * @param sortType 정렬 조건
     * @param pageable 페이징 정보
     * @return question, interview까지 페치 조인하여 전체 답변 리스트를 반환
     */
    public Slice<Answer> findAnswers(
        Long memberId,
        Boolean isUnderstood,
        AnswerStatus status,
        String sortType,
        Pageable pageable
    ) {
        QAnswer answer = QAnswer.answer;
        QQuestion question = QQuestion.question;
        QInterview interview = QInterview.interview;

        // BooleanExpression 필터 적용
        BooleanExpression filter = buildFilter(memberId, isUnderstood, status);
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(sortType, answer);

        // QueryDSL 실행
        List<Answer> results = queryFactory
            .selectFrom(answer)
            .leftJoin(question).on(answer.questionId.eq(question.id)).fetchJoin()
            .leftJoin(interview).on(answer.interviewId.eq(interview.id)).fetchJoin()
            .where(filter)
            .orderBy(orderSpecifier)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1) // hasNext 확인을 위해 +1 조회
            .fetch();

        return toSlice(results, pageable);
    }

    // slice 리턴 형식을 맞추기 위한 메서드
    private Slice<Answer> toSlice(List<Answer> results, Pageable pageable) {
        boolean hasNext = results.size() > pageable.getPageSize();
        if (hasNext) {
            results.remove(results.size() - 1); // 추가로 가져온 항목 제거
        }
        return new SliceImpl<>(results, pageable, hasNext);
    }

    // 각 필터 생성
    private BooleanExpression buildFilter(Long memberId, Boolean isUnderstood, AnswerStatus status) {
        QAnswer answer = QAnswer.answer;

        BooleanExpression memberFilter = answer.memberId.eq(memberId);
        BooleanExpression understoodFilter = (isUnderstood != null) ? answer.isUnderstood.eq(isUnderstood) : null;
        BooleanExpression statusFilter = getStatusFilter(status);

        return allOf(memberFilter, understoodFilter, statusFilter);
    }

    // ALL 일때는 오답노트 밖에 없어 이렇게 설정. 다른 메서드에서는 ALL 일때 전체 다 넣으면 될 듯
    private BooleanExpression getStatusFilter(AnswerStatus status) {
        QAnswer answer = QAnswer.answer;

        if (status == null) return null;
        if (status == AnswerStatus.ALL) {
            return answer.status.in(AnswerStatus.INCORRECT, AnswerStatus.SKIPPED);
        }
        return answer.status.eq(status);
    }

    // 여러 BooleanExpression을 결합하는 헬퍼 메서드
    private BooleanExpression allOf(BooleanExpression... expressions) {
        BooleanExpression result = null;
        for (BooleanExpression expr : expressions) {
            if (expr != null) {
                result = (result == null) ? expr : result.and(expr);
            }
        }
        return result;
    }

    // 정렬 조건 생성 메서드
    private OrderSpecifier<?> getOrderSpecifier(String sortType, QAnswer answer) {
        return switch (sortType.toUpperCase()) {
            case "ASC" -> answer.createdAt.asc();
            case "DESC" -> answer.createdAt.desc();
            default -> throw new IllegalArgumentException("올바르지 않은 정렬 타입입니다: " + sortType);
        };
    }
}
