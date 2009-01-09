/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
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

  /**
   * Map of Participant.Interview.id to Map of Stage.id to StageExecutionContext
   */
  private Map<Serializable, Map<Serializable, StageExecutionContext>> interviewStageContexts;

  private Serializable currentParticipantId = null;

  private ModuleRegistry moduleRegistry;

  private ParticipantService participantService;

  public void setModuleRegistry(ModuleRegistry moduleRegistry) {
    this.moduleRegistry = moduleRegistry;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public Participant getParticipant() {
    if(currentParticipantId == null) return null;
    return getPersistenceManager().get(Participant.class, currentParticipantId);
  }

  public Interview getInterview() {
    Participant currentParticipant = getParticipant();
    if(currentParticipantId == null) return null;

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
    Participant currentParticipant = getParticipant();
    if(currentParticipant == null) return null;

    // try to find it in memory
    StageExecutionContext exec = retrieveStageExecutionContext(currentParticipant, stage);

    if(exec == null) {
      Module module = moduleRegistry.getModule(stage.getModule());
      exec = (StageExecutionContext) module.createStageExecution(currentParticipant.getInterview(), stage);

      for(StageExecutionContext sec : getStageExecutionContexts(currentParticipant)) {
        if(exec.getStage().getStageDependencyCondition() != null) {
          if(exec.getStage().getStageDependencyCondition().isDependentOn(sec.getStage().getName())) {
            sec.addTransitionListener(exec);
          }
        }
      }

      storeStageExecutionContext(currentParticipant, exec);

      // try to find previous state in memento
      StageExecutionMemento template = new StageExecutionMemento();
      template.setStage(stage.getName());
      template.setInterview(getInterview());
      StageExecutionMemento memento = getPersistenceManager().matchOne(template);
      if(memento != null) {
        exec.restoreFromMemento(memento);
      }
    }

    return exec;
  }

  public IStageExecution getStageExecution(String stageName) {
    Stage stage = moduleRegistry.getStage(stageName);
    if(stage == null) {
      log.warn("No stage with name '{}' is registered.", stageName);
      return null;
    }
    return getStageExecution(stage);
  }

  public void doAction(Stage stage, Action action, User user) {
    action.setInterview(getParticipant().getInterview());
    if(stage != null) {
      action.setStage(stage.getName());
    }
    action.setDateTime(new Date());
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
    this.currentParticipantId = participant.getId();
  }

  public Interview setInterviewOperator(User operator) {
    Participant currentParticipant = getParticipant();
    if(currentParticipant == null) return null;

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

  public void setStatus(InterviewStatus status) {
    Participant currentParticipant = getParticipant();
    if(currentParticipant == null) return;

    Interview template = new Interview();
    template.setParticipant(currentParticipant);
    Interview interview = getPersistenceManager().matchOne(template);

    if(interview != null) {
      interview.setStatus(status);
      if(status.equals(InterviewStatus.CANCELLED) || status.equals(InterviewStatus.COMPLETED)) {
        interview.setEndDate(new Date());
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

  public Action getStatusAction() {
    Interview interview = getInterview();
    Action template = new Action();
    template.setInterview(interview);
    switch(interview.getStatus()) {
    case CANCELLED:
      template.setActionType(ActionType.STOP);
      break;
    case COMPLETED:
      template.setActionType(ActionType.COMPLETE);
      break;
    }

    List<Action> actions = getPersistenceManager().match(template, new SortingClause("dateTime", false));

    if(actions.size() > 0) return actions.get(0);
    else
      return null;
  }

  public void storeStageExecutionContext(Participant participant, StageExecutionContext exec) {
    getInterviewStageExecutionContexts(participant).put(exec.getStage().getName(), exec);
  }

  public StageExecutionContext retrieveStageExecutionContext(Participant participant, Stage stage) {
    return getInterviewStageExecutionContexts(participant).get(stage.getName());
  }

  private Map<Serializable, StageExecutionContext> getInterviewStageExecutionContexts(Participant participant) {
    Map<Serializable, StageExecutionContext> contexts = interviewStageContexts.get(participant.getInterview().getId());
    if(contexts == null) {
      contexts = new HashMap<Serializable, StageExecutionContext>();
      interviewStageContexts.put(participant.getInterview().getId(), contexts);
    }
    return contexts;
  }

  public Collection<StageExecutionContext> getStageExecutionContexts(Participant participant) {
    return Collections.unmodifiableCollection(getInterviewStageExecutionContexts(participant).values());
  }

  public void setInterviewStageContexts(Map<Serializable, Map<Serializable, StageExecutionContext>> interviewStageContexts) {
    this.interviewStageContexts = interviewStageContexts;
  }
}
