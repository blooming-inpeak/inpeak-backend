package com.blooming.inpeak.question.repository;

import com.blooming.inpeak.question.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

}
