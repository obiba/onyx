/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

import org.obiba.onyx.quartz.core.engine.questionnaire.answer.DataSource;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.Data;

/**
 * Compares two {@link Data} provided by {@link DataSource}.
 */
public class DataCondition extends Condition {

  private static final long serialVersionUID = -7608048954030186313L;

  private DataSource dataSource1;

  private DataSource dataSource2;

  private ComparisonOperator comparisonOperator;

  public DataCondition(String name, DataSource dataSource1, ComparisonOperator comparisonOperator, DataSource dataSource2) {
    super(name);
    this.dataSource1 = dataSource1;
    this.dataSource2 = dataSource2;
    this.comparisonOperator = comparisonOperator;
  }

  public boolean isToBeAnswered(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {
    Data data1 = dataSource1.getData(activeQuestionnaireAdministrationService);
    Data data2 = dataSource2.getData(activeQuestionnaireAdministrationService);

    if(data1 == null && data2 == null) {
      return isComparisonValid(0);
    } else if(data1 != null) {
      return isComparisonValid(data1.compareTo(data2));
    } else {
      return false;
    }
  }

  protected boolean isComparisonValid(int result) {

    switch(comparisonOperator) {
    case eq:
      return result == 0;
    case ne:
      return result != 0;
    case lt:
      return result < 0;
    case le:
      return result <= 0;
    case gt:
      return result > 0;
    case ge:
      return result >= 0;
    default:
      return false;
    }
  }

}
