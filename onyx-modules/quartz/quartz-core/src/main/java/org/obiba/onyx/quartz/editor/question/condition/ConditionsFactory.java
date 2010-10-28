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

import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.util.data.Data;

/**
 *
 */
public class ConditionsFactory {

  // private final transient Logger log = LoggerFactory.getLogger(getClass());

  public static Conditions create(Question question) {
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
      } else if(conditionDataSource instanceof ComparingDataSource) {
        ComparingDataSource comparingDataSource = (ComparingDataSource) conditionDataSource;
        IDataSource left = comparingDataSource.getDataSourceLeft();
        IDataSource right = comparingDataSource.getDataSourceRight();
        if(left instanceof VariableDataSource && right instanceof FixedDataSource) {
          Data data = ((FixedDataSource) right).getData(null);
          VariableDS ds = createVariableDS((VariableDataSource) left);
          ds.setOperator(comparingDataSource.getComparisonOperator());
          ds.setType(data.getType());
          ds.setValue(data.getValueAsString());
          conditions.getVariables().add(ds);
          ds.setIndex(conditions.getVariables().size());
        }
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
