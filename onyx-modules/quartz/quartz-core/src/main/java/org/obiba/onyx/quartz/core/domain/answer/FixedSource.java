package org.obiba.onyx.quartz.core.domain.answer;

public class FixedSource extends AnswerSource {

	private String value;

	public FixedSource() {
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
