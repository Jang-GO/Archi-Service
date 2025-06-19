package com.archiservice.survey.domain;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="survey_questions")
public class Question {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long surveyQuestionId;
	private String questionText;
	private int questionOrder;
	
	@OneToMany(mappedBy = "surveyQuestion", fetch = FetchType.EAGER)
	private List<Option> options;

}
