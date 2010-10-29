/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question.condition;

import org.obiba.magma.Variable;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;

/**
 *
 */
public class ConditionsFactory {

  // private final transient Logger log = LoggerFactory.getLogger(getClass());

  public static Conditions create(Question question, Questionnaire questionnaire) {
    Conditions conditions = new Conditions();
    IDataSource conditionDataSource = question.getCondition();
    if(conditionDataSource != null) {
      if(conditionDataSource instanceof ComputingDataSource) {
        ComputingDataSource computingDataSource = (ComputingDataSource) conditionDataSource;
        conditions.setExpression(computingDataSource.getExpression());
        int index = 1;
        for(IDataSource dataSource : computingDataSource.getDataSources()) {
          if(dataSource instanceof VariableDataSource) {
            VariableDS ds = createVariableDS((VariableDataSource) dataSource);
            ds.setIndex(index++);
            conditions.getVariables().add(ds);
          }
        }
      } else if(conditionDataSource instanceof VariableDataSource) {
        VariableDataSource variableDataSource = (VariableDataSource) conditionDataSource;
        if(questionnaire.getQuestionnaireCache() == null) {
          QuestionnaireFinder.getInstance(questionnaire).buildQuestionnaireCache();
        }
        Variable variable = questionnaire.getQuestionnaireCache().getVariableCache().get(variableDataSource.getVariableName());
        VariableDS ds = createVariableDS(variableDataSource);
        ds.setScript(variable.getAttributeStringValue("script"));
        conditions.getVariables().add(ds);
        ds.setIndex(conditions.getVariables().size());
      }
    }

    return conditions;
  }

  private static VariableDS createVariableDS(VariableDataSource variableDataSource) {
    VariableDS ds = new VariableDS();
    ds.setTable(variableDataSource.getTableName());
    ds.setName(variableDataSource.getVariableName());
    return ds;
  }

}
