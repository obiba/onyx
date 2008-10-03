package org.obiba.onyx.quartz.core.domain.condition;

import java.io.Serializable;

import org.obiba.onyx.util.data.Data;

public class DataComparator implements Serializable {

	private static final long serialVersionUID = 6128481252934955909L;

	private ComparisionOperator comparisionOperator;
	
	private Data data;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
	
	public ComparisionOperator getComparisionOperator() {
		return comparisionOperator;
	}

	public void setComparisionOperator(ComparisionOperator comparisionOperator) {
		this.comparisionOperator = comparisionOperator;
	}	
	
}
