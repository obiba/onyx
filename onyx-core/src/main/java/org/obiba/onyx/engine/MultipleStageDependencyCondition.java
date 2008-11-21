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

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows composing two instances of {@link StageDependencyCondition} using a boolean operator.
 * <p>
 * The normal boolean logic applies for determining the return value. The notable cases are shown in the following
 * table. <table>
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
 * </table> For the OR operator, null is returned whenever one of the two conditions is returns null and the other is
 * false. The reason being that one returning null may return true on subsequent calls.
 */
public class MultipleStageDependencyCondition implements StageDependencyCondition {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(MultipleStageDependencyCondition.class);

  public enum Operator {
    AND, OR;
  }

  private Operator operator;

  private StageDependencyCondition leftStageDependencyCondition;

  private StageDependencyCondition rightStageDependencyCondition;

  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    Boolean leftResult = leftStageDependencyCondition.isDependencySatisfied(activeInterviewService);
    Boolean rightResult = rightStageDependencyCondition.isDependencySatisfied(activeInterviewService);

    log.info("{}.isDependencySatisfied: {} {} {}", new Object[] { toString(), leftResult, operator, rightResult });

    if(leftResult != null && rightResult != null) {
      if(operator == Operator.AND) {
        return (leftResult && rightResult);
      } else {
        return (leftResult || rightResult);
      }
    } else {
      if(operator == Operator.AND) {
        if(isFalse(leftResult) || isFalse(rightResult)) {
          return false;
        }
        return null;
      } else {
        if(isTrue(leftResult) || isTrue(rightResult)) {
          return true;
        }
        return null;
      }
    }
  }

  public boolean isDependentOn(String stageName) {
    return (leftStageDependencyCondition.isDependentOn(stageName) || rightStageDependencyCondition.isDependentOn(stageName));
  }

  public void setLeftStageDependencyCondition(StageDependencyCondition leftStageDependencyCondition) {
    this.leftStageDependencyCondition = leftStageDependencyCondition;
  }

  public void setRightStageDependencyCondition(StageDependencyCondition rightStageDependencyCondition) {
    this.rightStageDependencyCondition = rightStageDependencyCondition;
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
    return "[" + getClass().getSimpleName() + ":" + leftStageDependencyCondition + ":" + operator + ":" + rightStageDependencyCondition + "]";
  }

}
