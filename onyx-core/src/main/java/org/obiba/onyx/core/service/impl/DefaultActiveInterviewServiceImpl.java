package org.obiba.onyx.core.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.stage.StageExecutionMemento;
import org.obiba.onyx.core.domain.user.User;
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
    if(currentParticipant == null) return null;

    Interview interview = currentParticipant.getInterview();

    if(interview == null) {
      interview = new Interview();
      interview.setParticipant(currentParticipant);
      interview.setStartDate(new Date());
      interview.setStatus(InterviewStatus.IN_PROGRESS);
      getPersistenceManager().save(interview);
      currentParticipant = getPersistenceManager().get(Participant.class, currentParticipant.getId());
      interview = currentParticipant.getInterview();
    }

    return interview;
  }

  public IStageExecution getStageExecution(Stage stage) {
    if(currentParticipant == null) return null;

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
      if(stage.getDependsOnStages().size() > 0) {
        List<IStageExecution> dependsOnStageList = new ArrayList<IStageExecution>();
        for(Stage dependsOnStage : stage.getDependsOnStages()) {
          dependsOnStageList.add(getStageExecution(dependsOnStage));
        }
        exec = (StageExecutionContext) module.createStageExecution(getInterview(), stage, dependsOnStageList.toArray(new IStageExecution[dependsOnStageList.size()]));
      } else {
        exec = (StageExecutionContext) module.createStageExecution(getInterview(), stage);
      }

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

  public void doAction(Stage stage, Action action, User user) {
    action.setInterview(getParticipant().getInterview());
    action.setStage(stage);
    action.setDateTime(new Date());
    // TODO add user etc.
    action.setUser(user);
    getPersistenceManager().save(action);

    if(stage != null) {
      IStageExecution exec = getStageExecution(stage);
      action.getActionType().act(exec, action);
    }
  }

  public void shutdown() {
    log.info("shutdown");
  }

  public void setParticipant(Participant participant) {
    this.currentParticipant = participant;
  }

  public Interview setInterviewOperator(User operator) {
    Interview template = new Interview();
    template.setParticipant(currentParticipant);
    Interview interview = getPersistenceManager().matchOne(template);

    if(interview != null) {
      interview.setUser(operator);
      getPersistenceManager().save(interview);
      currentParticipant = getPersistenceManager().get(Participant.class, currentParticipant.getId());
    }

    return interview;
  }

  public void setStatus(InterviewStatus status, Date stopDate) {
    Interview template = new Interview();
    template.setParticipant(currentParticipant);
    Interview interview = getPersistenceManager().matchOne(template);

    if(interview != null) {
      interview.setStatus(status);
      if(stopDate != null) {
        interview.setStopDate(stopDate);
      }
      getPersistenceManager().save(interview);
    }
  }

  public List<Action> getInterviewComments() {
    Action template = new Action();
    template.setInterview(getInterview());
    List<Action> actions = getPersistenceManager().match(template, new SortingClause("dateTime", false));
    List<Action> comments = new ArrayList<Action>();

    for(Action action : actions) {
      if(action.getComment() != null) {
        comments.add(action);
      }
    }

    return comments;
  }

}
