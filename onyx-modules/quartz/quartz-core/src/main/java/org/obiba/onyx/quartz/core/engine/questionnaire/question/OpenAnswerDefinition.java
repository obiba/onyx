package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public class OpenAnswerDefinition implements Serializable, ILocalizable {

	private static final long serialVersionUID = -7756577128502621726L;

	private String name;

	private DataType dataType;

	private String unit;

	private String format;

	private String absoluteMinValue;

	private String absoluteMaxValue;

	private String usualMinValue;

	private String usualMaxValue;

	private Data defaultData;

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

	public Data getDefaultData() {
		return defaultData;
	}

	public void setDefaultData(Data defaultData) {
		this.defaultData = defaultData;
	}

	private static final String[] PROPERTIES = { "label", "unitLabel" };

  public String getPropertyKey(String property) {
    for(String key : PROPERTIES) {
      if(key.equals(property)) {
        return getClass().getSimpleName() + "." + getName() + "." + property;
      }
    }
    throw new IllegalArgumentException("Invalid property for class " + getClass().getName() + ": " + property);
  }
  
  public String[] getProperties() {
    return PROPERTIES;
  }

}
