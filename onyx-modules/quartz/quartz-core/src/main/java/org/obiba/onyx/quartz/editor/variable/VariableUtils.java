/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.variable;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnaireConverterException;
import org.obiba.onyx.util.data.DataType;
import org.springframework.beans.factory.annotation.Required;

/**
 *
 */
public class VariableUtils {

  // private final transient Logger logger = LoggerFactory.getLogger(getClass());

  public static final String ONYX_DATASOURCE = "onyx-datasource";

  public static final String QUESTION_NAME = "questionName";

  public static final String CATEGORY_NAME = "categoryName";

  private QuestionnaireBundleManager questionnaireBundleManager;

  public Variable findVariable(VariableDataSource variableDataSource) {
    Variable variable = null;
    String tableName = variableDataSource.getTableName();
    String variableName = variableDataSource.getVariableName();

    try {
      ValueTable valueTable = MagmaEngine.get().getDatasource(ONYX_DATASOURCE).getValueTable(tableName);
      variable = valueTable == null ? null : valueTable.getVariable(variableName);
    } catch(NoSuchValueTableException e) {
    } catch(NoSuchVariableException e) {
    }
    if(variable == null) {
      QuestionnaireBundle bundle = questionnaireBundleManager.getBundle(tableName);
      if(bundle != null) {
        try {
          variable = bundle.getQuestionnaire().getVariable(variableName);
        } catch(IllegalArgumentException e) {
        }
      }
    }
    return variable;
  }

  public static Question findQuestion(Variable variable, QuestionnaireFinder questionnaireFinder) {
    try {
      if(variable.hasAttribute(QUESTION_NAME)) {
        return questionnaireFinder.findQuestion(variable.getAttributeStringValue(QUESTION_NAME));
      }
    } catch(NoSuchAttributeException e) {
      return null;
    }
    return null;
  }

  public static Category findCategory(Variable variable, Question question) {
    try {
      if(variable.hasAttribute(CATEGORY_NAME)) {
        String categoryName = variable.getAttributeStringValue(CATEGORY_NAME);
        if(question.getQuestionCategories().isEmpty()) {
          if(question.getParentQuestion() != null) { // get categories from parent (for array)
            return question.getParentQuestion().getCategoriesByName().get(categoryName);
          }
        } else {
          return question.getCategoriesByName().get(categoryName);
        }
      }
    } catch(NoSuchAttributeException e) {
      return null;
    }
    return null;
  }

  public static Iterable<Variable> findVariable(Questionnaire questionnaire) {
    return MagmaEngine.get().getDatasource(ONYX_DATASOURCE).getValueTable(questionnaire.getName()).getVariables();
  }

  public static ValueType convertToValueType(DataType dataType) {
    switch(dataType) {
    case DECIMAL:
      return DecimalType.get();
    case INTEGER:
      return IntegerType.get();
    case BOOLEAN:
      return BooleanType.get();
    case TEXT:
      return TextType.get();
    case DATE:
      return DateType.get();
    default:
      throw new QuestionnaireConverterException("Type[" + dataType + "] is not supported");
    }
  }

  @Required
  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }

}
