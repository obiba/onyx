/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

/**
 * 
 */
public abstract class AbstractDataComparator implements IDataComparator {

  private ComparisionOperator comparisionOperator;

  public AbstractDataComparator(ComparisionOperator comparisionOperator) {
    super();
    this.comparisionOperator = comparisionOperator;
  }

  public ComparisionOperator getComparisionOperator() {
    return comparisionOperator;
  }

  public void setComparisionOperator(ComparisionOperator comparisionOperator) {
    this.comparisionOperator = comparisionOperator;
  }

  protected boolean isComparisonValid(int result) {

    switch(getComparisionOperator()) {
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
