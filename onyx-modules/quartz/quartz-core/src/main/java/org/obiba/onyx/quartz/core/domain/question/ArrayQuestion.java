package org.obiba.onyx.quartz.core.domain.question;

import java.util.ArrayList;
import java.util.List;

public class ArrayQuestion extends Question {

	private List<ChoiceQuestion> choiceQuestions;

	public ArrayQuestion() {
	}

	public List<ChoiceQuestion> getChoiceQuestions() {
		return choiceQuestions != null ? choiceQuestions : (choiceQuestions = new ArrayList<ChoiceQuestion>());
	}
	
	public void addQuestion(ChoiceQuestion choiceQuestions) {
		if (choiceQuestions != null) {
			getChoiceQuestions().add(choiceQuestions);
			choiceQuestions.setArrayQuestion(this);
		}
	}

}
