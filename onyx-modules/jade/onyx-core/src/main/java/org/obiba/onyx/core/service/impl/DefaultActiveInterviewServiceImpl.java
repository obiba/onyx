package org.obiba.onyx.core.service.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.IMemento;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.stage.StageExecutionMemento;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultActiveInterviewServiceImpl extends PersistenceManagerAwareService implements ActiveInterviewService {

  private static final Logger log = LoggerFactory.getLogger(DefaultActiveInterviewServiceImpl.class);

  private Participant currentParticipant = null;

  private ModuleRegistry moduleRegistry;

  private Map<Serializable, Map<Serializable, StageExecutionContext>> interviewStageContexts = new HashMap<Serializable, Map<Serializable, StageExecutionContext>>();

  public void setModuleRegistry(ModuleRegistry moduleRegistry) {
    this.moduleRegistry = moduleRegistry;
  }

  public Participant getParticipant() {
    return currentParticipant;
  }

  public Interview getInterview() {
    if(currentParticipant.getInterview() == null) {
      Interview interview = new Interview();
      interview.setParticipant(currentParticipant);
      interview.setStartDate(new Date());
      interview.setStatus(InterviewStatus.IN_PROGRESS);
      getPersistenceManager().save(interview);
      currentParticipant = getPersistenceManager().refresh(currentParticipant);
    }

    return currentParticipant.getInterview();
  }

  public IStageExecution getStageExecution(Stage stage) {

    // try to find it in memory
    Interview interview = getInterview();
    Map<Serializable, StageExecutionContext> contexts = interviewStageContexts.get(interview.getId());
    if(contexts == null) {
      contexts = new HashMap<Serializable, StageExecutionContext>();
      interviewStageContexts.put(interview.getId(), contexts);
    }
    StageExecutionContext exec = contexts.get(stage.getId());

    if(exec == null) {

      Module module = moduleRegistry.getModule(stage.getModule());
      exec = (StageExecutionContext) module.createStageExecution(getInterview(), stage);
      contexts.put(stage.getId(), exec);

      // try to find it in memory
      StageExecutionMemento template = new StageExecutionMemento();
      template.setStage(stage);
      template.setInterview(getInterview());
      StageExecutionMemento memento = getPersistenceManager().matchOne(template);
      if(memento != null) {
        exec.restoreFromMemento(memento);
      }
    }

    return exec;
  }

  public void doAction(Stage stage, Action action) {
    action.setInterview(getParticipant().getInterview());
    action.setStage(stage);
    action.setDateTime(new Date());
    // TODO add user etc.
    getPersistenceManager().save(action);

    IStageExecution exec = getStageExecution(stage);
    action.getActionType().act(exec, action);

    // persist in memento
    if(exec instanceof IMemento) {
      StageExecutionMemento template = new StageExecutionMemento();
      template.setStage(stage);
      template.setInterview(action.getInterview());
      StageExecutionMemento memento = (StageExecutionMemento) ((IMemento) exec).saveToMemento(getPersistenceManager().matchOne(template));
      getPersistenceManager().save(memento);
    }

  }

  public void shutdown() {
    log.info("shutdown");
    // for (Serializable interviewId : interviewStageContexts.keySet()) {
    // Map<Serializable, StageExecutionContext> contexts = interviewStageContexts.get(interviewId);
    // for (Serializable stageId : contexts.keySet()) {
    // // persist in a memento
    // StageExecutionMemento template = new StageExecutionMemento();
    // template.setStage(getPersistenceManager().get(Stage.class, stageId));
    // template.setInterview(getPersistenceManager().get(Interview.class, interviewId));
    // StageExecutionContext exec = contexts.get(stageId);
    // StageExecutionMemento memento =
    // (StageExecutionMemento)exec.saveToMemento(getPersistenceManager().matchOne(template));
    // getPersistenceManager().save(memento);
    // }
    // }
  }

  public void setParticipant(Participant participant) {
    this.currentParticipant = participant;
  }

}
