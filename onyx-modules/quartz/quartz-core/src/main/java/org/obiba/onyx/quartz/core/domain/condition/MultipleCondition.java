package org.obiba.onyx.quartz.core.domain.condition;


public class MultipleCondition extends Condition {

	private Condition condition1;

	private Condition condition2;

	private ConditionOperator conditionOperator;

	public MultipleCondition() {
	}

	public Condition getCondition1() {
		return condition1;
	}

	public void setCondition1(Condition condition1) {
		this.condition1 = condition1;
	}

	public Condition getCondition2() {
		return condition2;
	}

	public void setCondition2(Condition condition2) {
		this.condition2 = condition2;
	}

	public ConditionOperator getConditionOperator() {
		return conditionOperator;
	}

	public void setConditionOperator(ConditionOperator conditionOperator) {
		this.conditionOperator = conditionOperator;
	}

}
