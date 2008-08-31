package org.obiba.onyx.quartz.core.domain.condition;

import org.obiba.onyx.quartz.core.domain.question.CodeAnswer;
import org.obiba.onyx.quartz.core.domain.question.Question;
import org.obiba.onyx.util.data.DataType;

public class SingleCondition extends Condition {

	private Question question;

	private Integer occurence;

	private CodeAnswer codeAnswer;

	private DataType dataType;

	private String textValue;

	private Long integerValue;

	private Double decimalValue;

	private ComparisionOperator comparisionOperator;

	public SingleCondition() {
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Integer getOccurence() {
		return occurence;
	}

	public void setOccurence(Integer occurence) {
		this.occurence = occurence;
	}

	public CodeAnswer getCodeAnswer() {
		return codeAnswer;
	}

	public void setCodeAnswer(CodeAnswer codeAnswer) {
		this.codeAnswer = codeAnswer;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public String getTextValue() {
		return textValue;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	public Long getIntegerValue() {
		return integerValue;
	}

	public void setIntegerValue(Long integerValue) {
		this.integerValue = integerValue;
	}

	public Double getDecimalValue() {
		return decimalValue;
	}

	public void setDecimalValue(Double decimalValue) {
		this.decimalValue = decimalValue;
	}

	public ComparisionOperator getComparisionOperator() {
		return comparisionOperator;
	}

	public void setComparisionOperator(ComparisionOperator comparisionOperator) {
		this.comparisionOperator = comparisionOperator;
	}

}
