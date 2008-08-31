package org.obiba.onyx.quartz.core.domain.question;

import java.util.ArrayList;
import java.util.List;

public class IterativeQuestion extends Question {

	private Integer minIteration;

	private Integer maxIteration;

	private Integer currentIteration;

	private Integer lastIteration;

	private List<Question> questions;
	
	public IterativeQuestion() {
	}

	public Integer getMinIteration() {
		return minIteration;
	}

	public void setMinIteration(Integer minIteration) {
		this.minIteration = minIteration;
	}

	public Integer getMaxIteration() {
		return maxIteration;
	}

	public void setMaxIteration(Integer maxIteration) {
		this.maxIteration = maxIteration;
	}

	public Integer getCurrentIteration() {
		return currentIteration;
	}

	public void setCurrentIteration(Integer currentIteration) {
		this.currentIteration = currentIteration;
	}

	public Integer getLastIteration() {
		return lastIteration;
	}

	public void setLastIteration(Integer lastIteration) {
		this.lastIteration = lastIteration;
	}

	public List<Question> getQuestions() {
		return questions != null ? questions : (questions = new ArrayList<Question>());
	}
	
	public void addQuestion(Question question) {
		if (question != null) {
			getQuestions().add(question);
		}
	}
	
}
