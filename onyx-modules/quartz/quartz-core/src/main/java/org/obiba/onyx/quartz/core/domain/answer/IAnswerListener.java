package org.obiba.onyx.quartz.core.domain.answer;

import org.obiba.onyx.quartz.core.domain.question.Question;

public interface IAnswerListener {

	public void onAnswer(Question source, ParticipantAnswer answer);
	
}
