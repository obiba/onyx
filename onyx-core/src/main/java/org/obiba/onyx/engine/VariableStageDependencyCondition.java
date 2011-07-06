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

import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.magma.DataValueConverter;
import org.obiba.onyx.magma.MagmaInstanceProvider;
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

  private MagmaInstanceProvider magmaInstanceProvider;

  private String stageName;

  private String variablePath;

  private ComparisonOperator operator;

  private Data data;

  public VariableStageDependencyCondition() {
  }

  public VariableStageDependencyCondition(String name) {
    this.stageName = name;
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
      IStageExecution stageExecution = activeInterviewService.getStageExecution(stageName);
      if(stageExecution != null) {
        if(stageExecution.isCompleted() == false) {
          return null;
        }
      }
    }

    Boolean rval = null;

    try {
      // Get the stage's ValueTable.
      ValueTable stageTable = magmaInstanceProvider.getValueTable(stageName);
      String magmaVariableName = variablePath.replaceFirst("Onyx.", "");
      VariableValueSource variableValueSource = stageTable.getVariableValueSource(magmaVariableName);

      log.debug("Testing variable: {} {} {}", new Object[] { magmaVariableName, operator != null ? operator : ComparisonOperator.eq, data != null ? data : "true" });

      // Get the currently interviewed participant's ValueSet.
      VariableEntity entity = magmaInstanceProvider.newParticipantEntity(activeInterviewService.getParticipant().getBarcode());
      if(stageTable.hasValueSet(entity)) {
        ValueSet valueSet = stageTable.getValueSet(entity);

        // Get the condition value.
        Value conditionValue = variableValueSource.getValue(valueSet);

        if(!conditionValue.isNull()) {
          if(conditionValue.isSequence()) {
            // apply an OR among the data of the variable
            for(Value partialConditionValue : conditionValue.asSequence().getValues()) {
              Data partialConditionData = DataValueConverter.valueToData(partialConditionValue);
              rval = compare(partialConditionData);
              if(rval != null && rval) {
                break;
              }
            }
          } else {
            Data conditionData = DataValueConverter.valueToData(conditionValue);
            rval = compare(conditionData);
          }
        }
      }
    } catch(NoSuchValueTableException e) {
      log.error("Stage {} depends on a stage that does not exist: {}", stage.getName(), stageName);
    } catch(NoSuchVariableException e) {
      log.error("Stage {} depends on a variable that does not exist: {}", stage.getName(), variablePath);
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

  public boolean isDependentOn(Stage stage, String stageName) {
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

  public void setMagmaInstanceProvider(MagmaInstanceProvider magmaInstanceProvider) {
    this.magmaInstanceProvider = magmaInstanceProvider;
  }

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + ":" + stageName + "." + variablePath + "]";
  }
}
