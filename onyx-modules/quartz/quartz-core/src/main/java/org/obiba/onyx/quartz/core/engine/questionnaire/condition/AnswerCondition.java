package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;

public class AnswerCondition extends Condition {

	private static final long serialVersionUID = -7608048954030186313L;

	private Question question;

	private Integer occurence;

	private Category category;
	
	private AnswerCondition parentAnswerCondition;

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

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public AnswerCondition getParentAnswerCondition() {
		return parentAnswerCondition;
	}

	public void setParentAnswerCondition(AnswerCondition parentAnswerCondition) {
		this.parentAnswerCondition = parentAnswerCondition;
	}

	public boolean isToBeAnswered() {
		// TODO Auto-generated method stub
		return true;
	}

}
