package org.obiba.onyx.quartz.core.domain.question;

import java.io.Serializable;
import java.util.Date;

import org.obiba.onyx.util.data.DataType;

public class OpenAnswerDefinition implements Serializable {
	
	private String name;

	private DataType dataType;

	private String unit;

	private String format;

	private String absoluteMinValue;

	private String absoluteMaxValue;

	private String usualMinValue;

	private String usualMaxValue;

	private String defaultTextValue;

	private Long defaultIntegerValue;

	private Double defaultDecimalValue;
	
	private Date defaultDateValue;

	public OpenAnswerDefinition() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getAbsoluteMinValue() {
		return absoluteMinValue;
	}

	public void setAbsoluteMinValue(String absoluteMinValue) {
		this.absoluteMinValue = absoluteMinValue;
	}

	public String getAbsoluteMaxValue() {
		return absoluteMaxValue;
	}

	public void setAbsoluteMaxValue(String absoluteMaxValue) {
		this.absoluteMaxValue = absoluteMaxValue;
	}

	public String getUsualMinValue() {
		return usualMinValue;
	}

	public void setUsualMinValue(String usualMinValue) {
		this.usualMinValue = usualMinValue;
	}

	public String getUsualMaxValue() {
		return usualMaxValue;
	}

	public void setUsualMaxValue(String usualMaxValue) {
		this.usualMaxValue = usualMaxValue;
	}

	public String getDefaultTextValue() {
		return defaultTextValue;
	}

	public void setDefaultTextValue(String defaultTextValue) {
		this.defaultTextValue = defaultTextValue;
	}

	public Long getDefaultIntegerValue() {
		return defaultIntegerValue;
	}

	public void setDefaultIntegerValue(Long defaultIntegerValue) {
		this.defaultIntegerValue = defaultIntegerValue;
	}

	public Double getDefaultDecimalValue() {
		return defaultDecimalValue;
	}

	public void setDefaultDecimalValue(Double defaultDecimalValue) {
		this.defaultDecimalValue = defaultDecimalValue;
	}

	public Date getDefaultDateValue() {
		return defaultDateValue;
	}

	public void setDefaultDateValue(Date defaultDateValue) {
		this.defaultDateValue = defaultDateValue;
	}

}
