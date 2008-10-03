package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Category implements Serializable, ILocalizable {

	private static final long serialVersionUID = -1722883141794376906L;

	private String name;

	private List<QuestionCategory> questionCodeAnswers;

	private OpenAnswerDefinition openAnswerDefinition;

	public Category() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<QuestionCategory> getQuestionCodeAnswers() {
		return questionCodeAnswers != null ? questionCodeAnswers
				: (questionCodeAnswers = new ArrayList<QuestionCategory>());
	}

	public void addQuestionCodeAnswer(QuestionCategory questionCodeAnswer) {
		if (questionCodeAnswer != null) {
			getQuestionCodeAnswers().add(questionCodeAnswer);
			questionCodeAnswer.setCodeAnswer(this);
		}
	}

	public OpenAnswerDefinition getOpenAnswerDefinition() {
		return openAnswerDefinition;
	}

	public void setOpenAnswerDefinition(
			OpenAnswerDefinition openAnswerDefinition) {
		this.openAnswerDefinition = openAnswerDefinition;
	}

	public String getPropertyKey(String property) {
		return getClass().getSimpleName() + "." + getName() + "." + property;
	}

}
