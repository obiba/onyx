package org.obiba.onyx.quartz.core.domain.answer;

import java.util.Date;

import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.quartz.core.domain.question.CodeAnswer;
import org.obiba.onyx.quartz.core.domain.question.Question;
import org.obiba.onyx.util.data.DataType;

@Entity
public class ParticipantAnswer extends AbstractEntity {

	private QuestionnaireParticipant questionnaireParticipant;
	
	private Question question;

	private Integer occurence;

	private CodeAnswer codeAnswer;

	private Question parentQuestion;

	private Boolean active;

	private String comment;

	private DataType dataType;

	private String textValue;

	private Long integerValue;

	private Double decimalValue;
	
	private Date dateValue;

	public ParticipantAnswer() {
	}

	public QuestionnaireParticipant getQuestionnaireParticipant() {
		return questionnaireParticipant;
	}

	public void setQuestionnaireParticipant(
			QuestionnaireParticipant questionnaireParticipant) {
		this.questionnaireParticipant = questionnaireParticipant;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Integer getOccurence() {
		return occurence;
	}

	public void setOccurence(Integer occurence) {
		this.occurence = occurence;
	}

	public CodeAnswer getCodeAnswer() {
		return codeAnswer;
	}

	public void setCodeAnswer(CodeAnswer codeAnswer) {
		this.codeAnswer = codeAnswer;
	}

	public Question getParentQuestion() {
		return parentQuestion;
	}

	public void setParentQuestion(Question parentQuestion) {
		this.parentQuestion = parentQuestion;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public String getTextValue() {
		return textValue;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	public Long getIntegerValue() {
		return integerValue;
	}

	public void setIntegerValue(Long integerValue) {
		this.integerValue = integerValue;
	}

	public Double getDecimalValue() {
		return decimalValue;
	}

	public void setDecimalValue(Double decimalValue) {
		this.decimalValue = decimalValue;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}
	
}
