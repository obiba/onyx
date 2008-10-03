package org.obiba.onyx.quartz.core.domain.question;

public class IterativeQuestion extends Question {

	private static final long serialVersionUID = 7966541836589614995L;

	private Integer minIteration;

	private Integer maxIteration;

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

}
