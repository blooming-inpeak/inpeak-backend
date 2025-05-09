package com.blooming.inpeak.question.repository;

import com.blooming.inpeak.question.domain.Question;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = """
        SELECT DISTINCT q.*
        FROM questions q
        LEFT JOIN answers a ON q.id = a.question_id AND a.member_id = :memberId
        WHERE q.type IN (:types)
        AND (a.id IS NULL OR a.is_understood = false)
        """, nativeQuery = true)
    List<Question> findFilteredQuestionsByTypes(@Param("types") List<String> types,
        @Param("memberId") Long memberId);
}
