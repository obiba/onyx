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

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.magma.DataValueConverter;
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

  private static final String PARTICIPANT_TABLE_NAME = "Participants";

  private static final String PARTICIPANT_ENTITY_TYPE = "Participant";

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

    // Get the Participant ValueTable.
    ValueTable onyxParticipantTable = null;
    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      onyxParticipantTable = datasource.getValueTable(PARTICIPANT_TABLE_NAME);
    }

    // Get the currently interviewed participant's ValueSet.
    VariableEntity entity = new VariableEntityBean(PARTICIPANT_ENTITY_TYPE, activeInterviewService.getParticipant().getBarcode());
    ValueSet valueSet = onyxParticipantTable.getValueSet(entity);

    // Get the condition value.
    VariableValueSource variableValueSource = onyxParticipantTable.getVariableValueSource(variablePath);
    Value conditionValue = variableValueSource.getValue(valueSet);
    Boolean rval = null;

    log.debug("Testing variable: {} {} {}", new Object[] { variablePath, operator != null ? operator : ComparisonOperator.eq, data != null ? data : "true" });
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

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + ":" + stageName + "." + variablePath + "]";
  }
}
