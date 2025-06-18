package com.archiservice.tendency.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.archiservice.tendency.survey.domain.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>{
}
