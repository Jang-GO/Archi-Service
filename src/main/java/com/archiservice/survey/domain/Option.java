package com.archiservice.survey.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "survey_options")
public class Option {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long surveyOptionId;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "survey_question_id")
	private Question surveyQuestion;
	
	private String optionText;
	private Long tagCode;
	private Long nextQuestionId;
	
}
