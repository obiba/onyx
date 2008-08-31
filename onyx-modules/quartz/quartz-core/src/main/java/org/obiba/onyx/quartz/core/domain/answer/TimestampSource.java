package org.obiba.onyx.quartz.core.domain.answer;

public class TimestampSource extends AnswerSource {

	private TimestampType timestampType;

	public TimestampSource() {
	}

	public TimestampType getTimestampType() {
		return timestampType;
	}

	public void setTimestampType(TimestampType timestampType) {
		this.timestampType = timestampType;
	}

}
