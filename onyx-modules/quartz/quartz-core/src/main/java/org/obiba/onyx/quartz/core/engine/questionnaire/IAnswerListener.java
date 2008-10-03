package org.obiba.onyx.quartz.core.engine.questionnaire;

import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;

public interface IAnswerListener {

	public void onAnswer(Question source, QuestionAnswer answer);
	
}
