package org.obiba.onyx.quartz.core.domain.answer;

import java.util.Date;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public class TimestampSource extends AnswerSource {

	private static final long serialVersionUID = 5049448952613044101L;
	
	private TimestampType timestampType;

	public TimestampSource() {
	}

	public TimestampType getTimestampType() {
		return timestampType;
	}

	public void setTimestampType(TimestampType timestampType) {
		this.timestampType = timestampType;
	}

	public Data getData() {
		return new Data(DataType.DATE, new Date());
	}

}
