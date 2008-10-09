package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * {@link OpenAnswerDefinition} builder, given a {@link Questionnaire} and a current {@link Category}.
 * @author Yannick Marcon
 *
 */
public class OpenAnswerDefinitionBuilder extends AbstractQuestionnaireElementBuilder<OpenAnswerDefinition> {

  /**
   * Constructor using {@link CategoryBuilder} to get the {@link Category} it is applied to.
   * @param parent
   * @param openAnswerDefinition
   */
  private OpenAnswerDefinitionBuilder(CategoryBuilder parent, OpenAnswerDefinition openAnswerDefinition) {
    super(parent.getQuestionnaire());
    element = openAnswerDefinition;
    parent.getElement().setOpenAnswerDefinition(element);
  }

  /**
   * Set the {@link OpenAnswerDefinition} to the current category.
   * @param name
   * @param dataType
   * @return
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  public static OpenAnswerDefinitionBuilder createOpenAnswerDefinition(CategoryBuilder parent, String name, DataType dataType) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    return new OpenAnswerDefinitionBuilder(parent, new OpenAnswerDefinition(name, dataType));
  }

  /**
   * Set the absolute range to the current {@link OpenAnswerDefinition}.
   * @param minValue no limit if null
   * @param maxValue no limit if null
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerDefinitionAbsoluteValues(Data minValue, Data maxValue) {
    element.setAbsoluteMinValue(minValue);
    element.setAbsoluteMaxValue(maxValue);

    return this;
  }

  /**
   * Set the absolute range to the current {@link OpenAnswerDefinition}.
   * @param minValue no limit if null or no length
   * @param maxValue no limit if null or no length
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerDefinitionAbsoluteValues(String minValue, String maxValue) {
    if(minValue != null && minValue.length() > 0) {
      element.setAbsoluteMinValue(DataBuilder.build(element.getDataType(), minValue));
    } else {
      element.setAbsoluteMinValue(null);
    }
    if(maxValue != null && maxValue.length() > 0) {
      element.setAbsoluteMaxValue(DataBuilder.build(element.getDataType(), maxValue));
    } else {
      element.setAbsoluteMaxValue(null);
    }

    return this;
  }

  /**
   * Set the usual range to the current {@link OpenAnswerDefinition}.
   * @param minValue no limit if null
   * @param maxValue no limit if null
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerDefinitionUsualValues(Data minValue, Data maxValue) {
    element.setUsualMinValue(minValue);
    element.setUsualMaxValue(maxValue);

    return this;
  }

  /**
   * Set the usual range to the current {@link OpenAnswerDefinition}.
   * @param minValue no limit if null or no length
   * @param maxValue no limit if null or no length
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerDefinitionUsualValues(String minValue, String maxValue) {
    if(minValue != null && minValue.length() > 0) {
      element.setUsualMinValue(DataBuilder.build(element.getDataType(), minValue));
    } else {
      element.setUsualMinValue(null);
    }
    if(maxValue != null && maxValue.length() > 0) {
      element.setUsualMaxValue(DataBuilder.build(element.getDataType(), maxValue));
    } else {
      element.setUsualMaxValue(null);
    }

    return this;
  }

  /**
   * Set the unit to the current {@link OpenAnswerDefinition}.
   * @param unit
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerDefinitionUnit(String unit) {
    element.setUnit(unit);

    return this;
  }

  /**
   * Set the data format to the current {@link OpenAnswerDefinition}.
   * @param format
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerDefinitionFormat(String format) {
    element.setFormat(format);

    return this;
  }

  /**
   * Set the default data to the current {@link OpenAnswerDefinition}.
   * @param data
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerDefinitionDefaultData(Data... defaultValues) {
    for(Data value : defaultValues) {
      element.addDefaultValue(value);
    }

    return this;
  }

  /**
   * Set the default data to the current {@link OpenAnswerDefinition}.
   * @param data
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerDefinitionDefaultData(String... defaultValues) {
    for(String value : defaultValues) {
      element.addDefaultValue(value);
    }

    return this;
  }

}
