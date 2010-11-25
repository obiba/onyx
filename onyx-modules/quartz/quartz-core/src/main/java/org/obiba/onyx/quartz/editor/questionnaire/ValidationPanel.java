/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.variable.VariableUtils;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.IDataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("serial")
public class ValidationPanel extends Panel {

  // TODO: localize date format
  public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @SpringBean
  private VariableUtils variableUtils;

  private final List<IModel<String>> errors = new ArrayList<IModel<String>>();

  private final List<IModel<String>> warnings = new ArrayList<IModel<String>>();

  private final QuestionnaireFinder questionnaireFinder;

  public ValidationPanel(String id, IModel<Questionnaire> model) {
    super(id, model);

    questionnaireFinder = QuestionnaireFinder.getInstance(model.getObject());
    questionnaireFinder.buildQuestionnaireCache();

    validate();

    add(CSSPackageResource.getHeaderContribution(ValidationPanel.class, "ValidationPanel.css"));

    add(new Label("explain", new ResourceModel("Explain")).setEscapeModelStrings(false));

    add(new ListView<IModel<String>>("item", errors) {
      protected void populateItem(ListItem<IModel<String>> item) {
        item.add(new Label("error", item.getModelObject()));
      }
    });

    add(new WebMarkupContainer("noErrors").setVisible(errors.isEmpty() && warnings.isEmpty()));
  }

  public void validate() {
    validateVariables();
    validateOpenAnswerValidators();
    validateDefaultValues();
    // TODO validate missing locale prop
  }

  private void error(String messageKey, Object... params) {
    errors.add(new StringResourceModel(messageKey, ValidationPanel.this, null, params));
  }

  private void validateVariables() {
    Questionnaire questionnaire = (Questionnaire) getDefaultModelObject();
    for(Variable variable : VariableUtils.findVariable(questionnaire)) {
      if(variable.hasAttribute(VariableUtils.QUESTION_NAME)) {
        String questionName = variable.getAttributeStringValue(VariableUtils.QUESTION_NAME);
        Question question = questionnaireFinder.findQuestion(questionName);
        if(question == null) {
          error("QuestionNotFound", questionName, variable.getName());
        } else {
          if(variable.hasAttribute(VariableUtils.CATEGORY_NAME)) {
            Category category = VariableUtils.findCategory(variable, question);
            if(category == null) {
              error("CategoryNotFound", variable.getAttributeStringValue(VariableUtils.CATEGORY_NAME), variable.getName());
            }
          }
        }
      }
    }
  }

  private void validateOpenAnswerValidators() {
    Questionnaire questionnaire = (Questionnaire) getDefaultModelObject();
    for(OpenAnswerDefinition openAnswer : questionnaire.getQuestionnaireCache().getOpenAnswerDefinitionCache().values()) {
      DataType dataType = openAnswer.getDataType();
      if(dataType == null) {
        error("UndefinedOpenAnswerType", openAnswer.getName());
      } else {
        ValueType valueType = VariableUtils.convertToValueType(dataType);
        for(IDataValidator<?> validator : openAnswer.getDataValidators()) {
          if(!dataType.equals(validator.getDataType())) {
            error("OpenAnswerTypeDifferentFromDataValidator", dataType, openAnswer.getName(), validator.getDataType());
          }
        }
        for(ComparingDataSource comparingDataSource : openAnswer.getValidationDataSources()) {
          VariableDataSource variableDataSource = (VariableDataSource) comparingDataSource.getDataSourceRight();
          Variable variable = variableUtils.findVariable(variableDataSource);
          // check variable type only for variable that are not a reference to a question category (because they are
          // always boolean)
          if(!variable.hasAttribute(VariableUtils.CATEGORY_NAME) && !valueType.equals(variable.getValueType())) {
            error("OpenAnswerTypeDifferentFromValidationVariable", dataType, openAnswer.getName(), variable.getName(), variable.getValueType().getClass().getSimpleName());
          }
        }
      }
    }
  }

  private void validateDefaultValues() {
    Questionnaire questionnaire = (Questionnaire) getDefaultModelObject();
    for(OpenAnswerDefinition openAnswer : questionnaire.getQuestionnaireCache().getOpenAnswerDefinitionCache().values()) {
      DataType dataType = openAnswer.getDataType();
      for(Data data : openAnswer.getDefaultValues()) {
        if(!isValidDefaultValue(dataType, data.getValueAsString())) {
          error("DefaultValueCannotBeCastToOpenAnswerType", data.getValueAsString(), openAnswer.getName(), dataType);
        }
      }
    }
  }

  public static boolean isValidDefaultValue(DataType dataType, String value) {
    switch(dataType) {
    case DATE:
      try {
        DATE_FORMAT.parse(value);
      } catch(ParseException nfe) {
        return false;
      }
      break;
    case DECIMAL:
      try {
        Double.parseDouble(value);
      } catch(NumberFormatException nfe) {
        return false;
      }
      break;
    case INTEGER:
      try {
        Long.parseLong(value);
      } catch(NumberFormatException nfe) {
        return false;
      }
      break;
    case TEXT:
      break;
    case BOOLEAN:
      break;
    case DATA:
      break;
    }
    return true;
  }

}
