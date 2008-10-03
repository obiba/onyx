package org.obiba.onyx.quartz.core.engine.questionnaire.answer;

import java.io.Serializable;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.util.data.Data;

public abstract class AnswerSource implements Serializable {

	private QuestionCategory questionCategory;

	public QuestionCategory getQuestionCategory() {
		return questionCategory;
	}

	public void setQuestionCategory(QuestionCategory questionCategory) {
		this.questionCategory = questionCategory;
	}

	/**
	 * Get the data for answer provisionning.
	 * @return
	 */
	public abstract Data getData();

}
