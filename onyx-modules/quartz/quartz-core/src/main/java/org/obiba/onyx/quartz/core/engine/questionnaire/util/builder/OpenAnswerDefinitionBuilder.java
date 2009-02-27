/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder;

import java.util.Calendar;

import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.CurrentDateSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.data.ParticipantPropertyDataSource;
import org.obiba.onyx.quartz.core.data.QuestionnaireDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultOpenAnswerDefinitionPanel;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.onyx.wicket.data.IDataValidator;

/**
 * {@link OpenAnswerDefinition} builder, given a {@link Questionnaire} and a current {@link Category}.
 * @author Yannick Marcon
 * 
 */
public class OpenAnswerDefinitionBuilder extends AbstractQuestionnaireElementBuilder<OpenAnswerDefinition> {

  /**
   * Constructor using {@link QuestionBuilder} to get the {@link Question} it is applied to.
   * @param parent
   * @param condition
   */
  private OpenAnswerDefinitionBuilder(QuestionnaireBuilder parent, OpenAnswerDefinition openAnswerDefinition) {
    super(parent);
    this.element = openAnswerDefinition;
  }

  /**
   * Constructor using {@link CategoryBuilder} to get the {@link Category} it is applied to.
   * @param parent
   * @param name
   * @param dataType
   */
  private OpenAnswerDefinitionBuilder(CategoryBuilder parent, String name, DataType dataType) {
    super(parent);
    if(!checkUniqueOpenAnswerDefinitionName(name)) {
      throw invalidNameUnicityException(OpenAnswerDefinition.class, name);
    }
    element = new OpenAnswerDefinition(name, dataType);
    element.setRequired(true);
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
    return new OpenAnswerDefinitionBuilder(parent, name, dataType);
  }

  /**
   * Add argument that will be interpreted by specific open answer UI.
   * @param key
   * @param value
   * @return
   */
  public OpenAnswerDefinitionBuilder addUIArgument(String key, String value) {
    element.addUIArgument(key, value);

    return this;
  }

  /**
   * Set the size of the open input field.
   * @param size
   * @return
   */
  public OpenAnswerDefinitionBuilder setSize(int size) {
    return addUIArgument(DefaultOpenAnswerDefinitionPanel.INPUT_SIZE_KEY, Integer.toString(size));
  }

  /**
   * Add a {@link IValidator} to the current {@link OpenAnswerDefinition}.
   * @param validator
   * @param dataType
   * @return
   */
  public OpenAnswerDefinitionBuilder addValidator(IValidator validator, DataType dataType) {
    element.addDataValidator(new DataValidator(validator, dataType));
    return this;
  }

  /**
   * Add a {@link IValidator} to the current {@link OpenAnswerDefinition}.
   * @param validator
   * @return
   */
  public OpenAnswerDefinitionBuilder addValidator(IValidator validator) {
    if(validator instanceof IDataValidator) {
      element.addDataValidator((IDataValidator) validator);
      return this;
    } else if(validator instanceof StringValidator) {
      return addValidator(validator, DataType.TEXT);
    } else {
      return addValidator(validator, element.getDataType());
    }
  }

  /**
   * Add a {@link IValidator} based on a {@link QuestionnaireDataSource} to the current {@link OpenAnswerDefinition}.
   * @param comparisonOperator
   * @param questionName
   * @param categoryName
   * @param openAnswerDefinitionName
   * @return
   */
  public OpenAnswerDefinitionBuilder addValidator(ComparisonOperator comparisonOperator, String questionName, String categoryName, String openAnswerDefinitionName) {
    element.addValidationDataSource(new ComparingDataSource(null, comparisonOperator, new QuestionnaireDataSource(getQuestionnaire().getName(), questionName, categoryName, openAnswerDefinitionName)));
    return this;
  }

  /**
   * Add a {@link IValidator} based on a {@link QuestionnaireDataSource} to the current {@link OpenAnswerDefinition}.
   * @param comparisonOperator
   * @param questionnaireName
   * @param questionName
   * @param categoryName
   * @param openAnswerDefinitionName
   * @return
   */
  public OpenAnswerDefinitionBuilder addValidator(ComparisonOperator comparisonOperator, String questionnaireName, String questionName, String categoryName, String openAnswerDefinitionName) {
    element.addValidationDataSource(new ComparingDataSource(null, comparisonOperator, new QuestionnaireDataSource(questionnaireName, questionName, categoryName, openAnswerDefinitionName)));
    return this;
  }

  /**
   * Add a {@link IValidator} based on a {@link ParticipantPropertyDataSource} to the current
   * {@link OpenAnswerDefinition}.
   * @param comparisonOperator
   * @param property
   * @return
   */
  public OpenAnswerDefinitionBuilder addValidator(ComparisonOperator comparisonOperator, String property) {
    element.addValidationDataSource(new ComparingDataSource(null, comparisonOperator, new ParticipantPropertyDataSource(property)));
    return this;
  }

  /**
   * Add a {@link IValidator} based on a {@link CurrentDateSource} to the current {@link OpenAnswerDefinition}.
   * @param comparisonOperator
   * @return
   */
  public OpenAnswerDefinitionBuilder addCurrentYearValidator(ComparisonOperator comparisonOperator) {
    element.addValidationDataSource(new ComparingDataSource(null, comparisonOperator, new CurrentDateSource(Calendar.YEAR)));
    return this;
  }

  /**
   * Add a {@link IValidator} based on a {@link FixedDataSource} to the current {@link OpenAnswerDefinition}.
   * @param comparisonOperator
   * @param data
   * @return
   */
  public OpenAnswerDefinitionBuilder addValidator(ComparisonOperator comparisonOperator, Data data) {
    element.addValidationDataSource(new ComparingDataSource(null, comparisonOperator, new FixedDataSource(data)));
    return this;
  }

  /**
   * Add a {@link IValidator} based on a {@link IDataSource} to the current {@link OpenAnswerDefinition}.
   * @param comparisonOperator
   * @param dataSource
   * @return
   * @see TimestampSource
   * @see CurrentYearSource
   */
  public OpenAnswerDefinitionBuilder addValidator(ComparisonOperator comparisonOperator, IDataSource dataSource) {
    element.addValidationDataSource(new ComparingDataSource(null, comparisonOperator, dataSource));
    return this;
  }

  /**
   * Set the required to the current {@link OpenAnswerDefinition}.
   * @param required
   * @return
   */
  public OpenAnswerDefinitionBuilder setRequired(boolean required) {
    element.setRequired(required);
    return this;
  }

  /**
   * Set the unit to the current {@link OpenAnswerDefinition}.
   * @param unit
   * @return
   */
  public OpenAnswerDefinitionBuilder setUnit(String unit) {
    element.setUnit(unit);
    return this;
  }

  /**
   * Set the default data to the current {@link OpenAnswerDefinition}.
   * @param data
   * @return
   */
  public OpenAnswerDefinitionBuilder setDefaultData(Data... defaultValues) {
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
  public OpenAnswerDefinitionBuilder setDefaultData(String... defaultValues) {
    for(String value : defaultValues) {
      element.addDefaultValue(value);
    }

    return this;
  }

  /**
   * All purpose data source.
   * @param dataSource
   * @return
   */
  public OpenAnswerDefinitionBuilder setDataSource(IDataSource dataSource) {
    element.setDataSource(dataSource);
    return this;
  }

  /**
   * Set a {@link CurrentDateSource} as to be the {@link IDataSource} for the current {@link OpenAnswerDefinition}.
   * @return
   */
  public OpenAnswerDefinitionBuilder setTimestampSource() {
    element.setDataSource(new CurrentDateSource());
    return this;
  }

  /**
   * Set a {@link QuestionnaireDataSource} as to be the {@link DataSource} for the current {@link OpenAnswerDefinition}.
   * @param questionName
   * @param categoryName
   * @param openAnswerDefinitionName
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerSource(String questionName, String categoryName, String openAnswerDefinitionName) {
    element.setDataSource(new QuestionnaireDataSource(getQuestionnaire().getName(), questionName, categoryName, openAnswerDefinitionName));
    return this;
  }

  /**
   * Set a {@link QuestionnaireDataSource} as to be the {@link DataSource} for the current {@link OpenAnswerDefinition}.
   * @param questionnaireName
   * @param questionName
   * @param categoryName
   * @param openAnswerDefinitionName
   * @return
   */
  public OpenAnswerDefinitionBuilder setExternalOpenAnswerSource(String questionnaireName, String questionName, String categoryName, String openAnswerDefinitionName) {
    element.setDataSource(new QuestionnaireDataSource(questionnaireName, questionName, categoryName, openAnswerDefinitionName));
    return this;
  }

  /**
   * Set a {@link ParticipantPropertyDataSource} as to be the {@link DataSource} for the current
   * {@link OpenAnswerDefinition}.
   * @param property
   * @return
   */
  public OpenAnswerDefinitionBuilder setParticipantPropertySource(String property) {
    element.setDataSource(new ParticipantPropertyDataSource(property));
    return this;
  }

  /**
   * Check open answer definition name unicity.
   * @param name
   * @return
   */
  private boolean checkUniqueOpenAnswerDefinitionName(String name) {
    return (QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition(name) == null);
  }

  /**
   * Add the {@link OpenAnswerDefinition} to the current openAnswerDefinition and set it as the current one.
   * @param name
   * @param dataType
   * @return
   */
  public OpenAnswerDefinitionBuilder withOpenAnswerDefinition(String name, DataType dataType) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }

    if(!checkUniqueOpenAnswerDefinitionName(name)) {
      throw invalidNameUnicityException(OpenAnswerDefinition.class, name);
    }

    OpenAnswerDefinition openAnswerDefinition = new OpenAnswerDefinition(name, dataType);
    openAnswerDefinition.setRequired(false);
    element.addOpenAnswerDefinition(openAnswerDefinition);
    element = openAnswerDefinition;
    return this;
  }

  /**
   * Set the given {@link OpenAnswerDefinition} as the current one.
   * @param questionnaire
   * @param condition
   * @return
   */
  public static OpenAnswerDefinitionBuilder inOpenAnswerDefinition(QuestionnaireBuilder parent, OpenAnswerDefinition openAnswerDefinition) {
    return new OpenAnswerDefinitionBuilder(parent, openAnswerDefinition);
  }
}
