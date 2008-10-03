package org.obiba.onyx.quartz.core.engine.questionnaire.answer;

import org.obiba.onyx.util.data.Data;

public class FixedSource extends AnswerSource {

	private static final long serialVersionUID = 1L;
	
	private Data data;

	public void setData(Data data) {
		this.data = data;
	}

	public Data getData() {
		return data;
	}

}
