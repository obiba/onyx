package org.obiba.onyx.quartz.core.domain.question;

import java.io.Serializable;

public class QuestionCodeAnswer implements Serializable {

	private Question question;

	private CodeAnswer codeAnswer;

	private Integer displayInQuestionOrder;

	private Boolean selected;

	public QuestionCodeAnswer() {

	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public CodeAnswer getCodeAnswer() {
		return codeAnswer;
	}

	public void setCodeAnswer(CodeAnswer codeAnswer) {
		this.codeAnswer = codeAnswer;
	}

	public Integer getDisplayInQuestionOrder() {
		return displayInQuestionOrder;
	}

	public void setDisplayInQuestionOrder(Integer displayInQuestionOrder) {
		this.displayInQuestionOrder = displayInQuestionOrder;
	}

	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

}
