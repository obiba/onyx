/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/

package org.obiba.onyx.mica.engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.engine.variable.IActionVariableProvider;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.IVariableProvider;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.mica.domain.conclusion.Conclusion;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class MicaModule implements Module, IVariableProvider, ApplicationContextAware {

  private static final Logger log = LoggerFactory.getLogger(MicaModule.class);

  private static final String ACCEPTED_ATTRIBUTE = "accepted";

  private static final String BARCODE_ATTRIBUTE = "barcode";

  private ApplicationContext applicationContext;

  private ActiveInterviewService activeInterviewService;

  private EntityQueryService queryService;

  private List<Stage> stages;

  public String getName() {
    return "mica";
  }

  public void initialize(WebApplication application) {
    log.info("initialize");
  }

  public void shutdown(WebApplication application) {
    log.info("shutdown");
  }

  public IStageExecution createStageExecution(Interview interview, Stage stage) {
    StageExecutionContext exec = (StageExecutionContext) applicationContext.getBean("stageExecutionContext");
    exec.setStage(stage);
    exec.setInterview(interview);

    AbstractStageState waiting = (AbstractStageState) applicationContext.getBean("micaWaitingState");
    AbstractStageState ready = (AbstractStageState) applicationContext.getBean("micaReadyState");
    AbstractStageState inProgress = (AbstractStageState) applicationContext.getBean("micaInProgressState");
    AbstractStageState completed = (AbstractStageState) applicationContext.getBean("micaCompletedState");
    AbstractStageState notApplicable = (AbstractStageState) applicationContext.getBean("micaNotApplicableState");

    exec.addEdge(ready, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(ready, TransitionEvent.START, inProgress);
    exec.addEdge(ready, TransitionEvent.CONTRAINDICATED, notApplicable);
    exec.addEdge(inProgress, TransitionEvent.CANCEL, ready);
    exec.addEdge(inProgress, TransitionEvent.COMPLETE, completed);
    exec.addEdge(inProgress, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(completed, TransitionEvent.CANCEL, ready);
    exec.addEdge(completed, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(completed, TransitionEvent.CONTRAINDICATED, notApplicable);

    exec.addEdge(notApplicable, TransitionEvent.VALID, ready);
    exec.addEdge(notApplicable, TransitionEvent.INVALID, waiting);

    exec.addEdge(waiting, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(waiting, TransitionEvent.CONTRAINDICATED, notApplicable);
    exec.addEdge(waiting, TransitionEvent.VALID, ready);
    exec.addEdge(ready, TransitionEvent.INVALID, waiting);
    exec.addEdge(completed, TransitionEvent.INVALID, waiting);

    if(stage.getStageDependencyCondition() == null) {
      exec.setInitialState(ready);
    } else {
      if(stage.getStageDependencyCondition().isDependencySatisfied(activeInterviewService) == null) {
        exec.setInitialState(waiting);
      } else if(stage.getStageDependencyCondition().isDependencySatisfied(activeInterviewService) == true) {
        exec.setInitialState(ready);
      }
    }
    return exec;
  }

  public List<Stage> getStages() {
    return stages;
  }

  public void setStages(List<Stage> stages) {
    this.stages = stages;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    VariableData varData = new VariableData(variablePathNamingStrategy.getPath(variable));

    Conclusion conclusion = new Conclusion();
    conclusion.setInterview(participant.getInterview());
    conclusion = queryService.matchOne(conclusion);

    if(conclusion != null) {
      if(variable.getName().equals(ACCEPTED_ATTRIBUTE)) {
        varData.addData(DataBuilder.buildBoolean(conclusion.isAccepted()));
      } else if(variable.getName().equals(BARCODE_ATTRIBUTE)) {
        varData.addData(DataBuilder.buildBoolean(conclusion.getBarcode()));
      }
    }

    return varData;
  }

  public List<Variable> getVariables() {
    List<Variable> variables = new ArrayList<Variable>();

    for(Stage stage : stages) {
      Variable stageVariable = new Variable(stage.getName());
      variables.add(stageVariable);

      stageVariable.addVariable(new Variable(ACCEPTED_ATTRIBUTE).setDataType(DataType.BOOLEAN));
      stageVariable.addVariable(new Variable(BARCODE_ATTRIBUTE).setDataType(DataType.TEXT));
    }
    return variables;
  }
}