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
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDirectory;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class VariableStageDependencyCondition implements StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(VariableStageDependencyCondition.class);

  private VariableDirectory variableDirectory;

  private String stageName;

  private String variablePath;

  private ComparisonOperator operator;

  private Data data;

  public VariableStageDependencyCondition() {
  }

  public VariableStageDependencyCondition(String name) {
    this.stageName = name;
  }

  public void setVariableDirectory(VariableDirectory variableDirectory) {
    this.variableDirectory = variableDirectory;
  }

  /**
   * Returns a Boolean depending on the fact that the step is completed and also on its result. Returns null if
   * dependent stage is not completed, otherwise the method returns the result of the comparison with a reference data
   * if a comparison operator is provided, else the value of the dependent stage's data if it is a boolean, or simply
   * the fact that the data is not null.
   */
  public Boolean isDependencySatisfied(Stage stage, ActiveInterviewService activeInterviewService) {
    // if stage is defined, check it is completed first
    if(stageName != null) {
      IStageExecution stageMaster = activeInterviewService.getStageExecution(stageName);
      if(stageMaster != null) {
        if(stageMaster.isInteractive()) {
          // ONYX-383 if slave is completed, it means that dependencies were already satisfied
          // then ignore the transition of the master that is being modified
          // and wait for the master to be in a stable state
          IStageExecution stageSlave = activeInterviewService.getStageExecution(stage);
          if(stageSlave.isCompleted()) {
            return true;
          } else {
            return null;
          }
        } else if(!stageMaster.isCompleted()) {
          return null;
        }
      }
    }

    Boolean rval = null;

    // ask variable directory
    log.debug("Testing variable: {} {} {}", new Object[] { variablePath, operator != null ? operator : ComparisonOperator.eq, data != null ? data : "true" });
    VariableData variableData = variableDirectory.getVariableData(activeInterviewService.getParticipant(), variablePath);
    if(variableData != null) {
      // apply a OR among the data of the variable
      for(Data varData : variableData.getDatas()) {
        log.debug("varData={}", varData);
        rval = compare(varData);
        if(rval != null && rval) {
          break;
        }
      }
    }

    // we need a comparison, by default variable data is false
    if(rval == null) {
      rval = compare(DataBuilder.buildBoolean(false));
    }

    log.debug("Test return value={}", rval);
    return rval;
  }

  private Boolean compare(Data varData) {
    Boolean rval;

    if(operator != null && data != null) {
      rval = operator.compare(varData, data);
    } else if(varData.getType().equals(DataType.BOOLEAN)) {
      rval = varData.getValue();
    } else {
      rval = (varData.getValue() != null);
    }

    return rval;
  }

  public boolean isDependentOn(String stageName) {
    return this.stageName.equals(stageName);
  }

  public String getStageName() {
    return stageName;
  }

  public void setStageName(String stageName) {
    this.stageName = stageName;
  }

  public String getVariablePath() {
    return variablePath;
  }

  public void setVariablePath(String variablePath) {
    this.variablePath = variablePath;
  }

  public ComparisonOperator getOperator() {
    return operator;
  }

  public void setOperator(ComparisonOperator operator) {
    this.operator = operator;
  }

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + ":" + stageName + "." + variablePath + "]";
  }

}
