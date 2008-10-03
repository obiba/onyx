package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

public class NoAnswerCondition extends Condition {

	private static final long serialVersionUID = -7934445960755750180L;
	
	private AnswerCondition answerCondition;

	public AnswerCondition getAnswerCondition() {
		return answerCondition;
	}

	public void setAnswerCondition(AnswerCondition answerCondition) {
		this.answerCondition = answerCondition;
	}

	public boolean isToBeAnswered() {
		return !answerCondition.isToBeAnswered();
	}

}
