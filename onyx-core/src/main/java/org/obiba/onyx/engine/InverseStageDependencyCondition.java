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

/**
 * Given an instance of {@link StageDependencyCondition} this class will return the opposite of its delegate. By
 * default, this class will return false when the delegate returns null. This can be overriden by setting the
 * {@link #valueWhenNull} attribute.
 */
public class InverseStageDependencyCondition implements StageDependencyCondition {

  private StageDependencyCondition stageDependencyCondition;

  private Boolean valueWhenNull = false;

  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    Boolean value = stageDependencyCondition.isDependencySatisfied(activeInterviewService);

    if(value == null) return valueWhenNull;
    return !value;
  }

  public boolean isDependentOn(String stageName) {
    return stageDependencyCondition.isDependentOn(stageName);
  }

  public void setStageDependencyCondition(StageDependencyCondition stageDependencyCondition) {
    this.stageDependencyCondition = stageDependencyCondition;
  }

  public void setValueWhenNull(Boolean valueWhenNull) {
    this.valueWhenNull = valueWhenNull;
  }

  public Boolean getValueWhenNull() {
    return valueWhenNull;
  }

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + ":" + valueWhenNull + "]";
  }
}
