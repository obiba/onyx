/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import java.util.List;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.BooleanType;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.magma.MagmaInstanceProvider;

/**
 * Stage dependency based on Magma javascript.
 */
public class JavascriptStageDependencyCondition implements StageDependencyCondition {

  private transient MagmaInstanceProvider magmaInstanceProvider;

  // List of stages that have to be completed before evaluating the script
  private List<String> stages;

  private String script;

  private ValueSource source;

  @Override
  public Boolean isDependencySatisfied(Stage stage, ActiveInterviewService activeInterviewService) {
    // if stage is defined, check it is completed first
    if(stages != null) {
      for(String stageName : stages) {
        IStageExecution stageExecution = activeInterviewService.getStageExecution(stageName);
        if(stageExecution != null) {
          if(stageExecution.isCompleted() == false) {
            return null;
          }
        }
      }
    }

    // Get the stage's ValueTable.
    ValueTable valueTable = magmaInstanceProvider.getValueTable(stage.getName());

    // Get the currently interviewed participant's ValueSet.
    VariableEntity entity = magmaInstanceProvider.newParticipantEntity(activeInterviewService.getParticipant().getBarcode());
    ValueSet valueSet = valueTable.getValueSet(entity);

    Value value = getSource().getValue(valueSet);
    return value.isNull() ? null : (Boolean) value.getValue();
  }

  @Override
  public boolean isDependentOn(Stage stage, String stageName) {
    if(stages != null) {
      for(String stageN : stages) {
        if(stageName.equals(stageN)) {
          return true;
        }
      }
    }
    return false;
  }

  public void setStages(List<String> stages) {
    this.stages = stages;
  }

  public void setScript(String script) {
    this.script = script;
    this.source = null;
  }

  public void setMagmaInstanceProvider(MagmaInstanceProvider magmaInstanceProvider) {
    this.magmaInstanceProvider = magmaInstanceProvider;
  }

  private ValueSource getSource() {
    if(source == null) {
      source = new JavascriptValueSource(BooleanType.get(), script);
      Initialisables.initialise(source);
    }
    return source;
  }

}
