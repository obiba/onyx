/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import java.util.List;

import org.obiba.core.util.StringUtil;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows composing two instances of {@link StageDependencyCondition} using a boolean operator.
 * <p>
 * The normal boolean logic applies for determining the return value. The notable cases are shown in the following
 * table.
 * <table>
 * <tr>
 * <td>Operator</td>
 * <td>Left</td>
 * <td>Right</td>
 * <td>Return Value</td>
 * </tr>
 * <tr>
 * <td rowspan="2">AND</td>
 * <td>null</td>
 * <td>false</td>
 * <td>false</td>
 * </tr>
 * <tr>
 * <td>false</td>
 * <td>null</td>
 * <td>false</td>
 * </tr>
 * 
 * <tr>
 * <td rowspan="2">OR</td>
 * <td>null</td>
 * <td>false</td>
 * <td>null</td>
 * </tr>
 * <tr>
 * <td>null</td>
 * <td>false</td>
 * <td>null</td>
 * </tr>
 * </table>
 * For the OR operator, null is returned whenever one of the two conditions is returns null and the other is false. The
 * reason being that one returning null may return true on subsequent calls.
 */
public class MultipleStageDependencyCondition implements StageDependencyCondition {

  private static final Logger log = LoggerFactory.getLogger(MultipleStageDependencyCondition.class);

  public enum Operator {
    AND, OR;
  }

  private Operator operator;

  private List<StageDependencyCondition> conditions;

  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    if(conditions == null) throw new IllegalStateException("conditions cannot be null");
    if(conditions.size() < 2) throw new IllegalStateException("at least 2 conditions required for MultipleStageDependencyCondition");
    if(operator == null) throw new IllegalStateException("operator cannot be null");

    if(log.isDebugEnabled()) {
      log.debug("{}.isDependencySatisfied: {}", new Object[] { toString(), StringUtil.collectionToString(conditions) });
    }

    StageDependencyCondition condition = conditions.get(0);
    Boolean returnValue = condition.isDependencySatisfied(activeInterviewService);
    for(int i = 1; i < conditions.size(); i++) {
      condition = conditions.get(i);

      Boolean left = returnValue;
      Boolean right = condition.isDependencySatisfied(activeInterviewService);

      switch(operator) {
      case AND:
        if(isTrue(left) && isTrue(right)) {
          returnValue = true;
        } else if(isFalse(left) || isFalse(right)) {
          returnValue = false;
        } else {
          returnValue = null;
        }
        break;
      case OR:
        if(isTrue(left) || isTrue(right)) {
          returnValue = true;
        } else if(isFalse(left) && isFalse(right)) {
          returnValue = false;
        } else {
          returnValue = null;
        }
        break;
      }
      log.debug("{}.isDependencySatisfied: {} {} {} -> {}", new Object[] { toString(), left, operator, right, returnValue });
    }
    return returnValue;
  }

  public boolean isDependentOn(String stageName) {
    for(StageDependencyCondition condition : this.conditions) {
      if(condition.isDependentOn(stageName)) {
        return true;
      }
    }
    return false;
  }

  public void setConditions(List<StageDependencyCondition> conditions) {
    this.conditions = conditions;
  }

  public void setOperator(Operator operator) {
    this.operator = operator;
  }

  /**
   * Returns true if {@code value} is not null and is false.
   * 
   * @param value the value to test
   * @return true when {@code value} is not null and is false.
   */
  private boolean isFalse(Boolean value) {
    return value != null && value == false;
  }

  /**
   * Returns true if {@code value} is not null and is true.
   * 
   * @param value the value to test
   * @return true when {@code value} is not null and is true.
   */
  private boolean isTrue(Boolean value) {
    return value != null && value == true;
  }

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + "(" + conditions.size() + "):" + operator + "]";
  }
}
