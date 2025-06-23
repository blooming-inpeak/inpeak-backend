package com.blooming.inpeak.answer.repository;

import com.blooming.inpeak.answer.domain.AnswerTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerTaskRepository extends JpaRepository<AnswerTask, Long> {

}
