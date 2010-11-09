/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.utils;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;

/**
 *
 */
public class VariableUtils {

  private static final String QUESTION_NAME = "questionName";

  private static final String CATEGORY_NAME = "categoryName";

  public static Variable findVariable(VariableDataSource variableDataSource) {
    try {
      for(Datasource datasource : MagmaEngine.get().getDatasources()) {
        ValueTable valueTable = datasource.getValueTable(variableDataSource.getTableName());
        if(valueTable != null) return valueTable.getVariable(variableDataSource.getVariableName());
      }
    } catch(NoSuchValueTableException e) {
      return null;
    } catch(NoSuchVariableException e) {
      return null;
    }
    return null;
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
        return question.getCategoriesByName().get(variable.getAttributeStringValue(CATEGORY_NAME));
      }
    } catch(NoSuchAttributeException e) {
      return null;
    }
    return null;
  }

}
