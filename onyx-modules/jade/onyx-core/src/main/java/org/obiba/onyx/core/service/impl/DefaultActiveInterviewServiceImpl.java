package org.obiba.onyx.core.service.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Interview;
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
  
  private ModuleRegistry moduleRegistry;
  
  private Map<Serializable, Map<Serializable, StageExecutionContext>> interviewStageContexts = new HashMap<Serializable, Map<Serializable, StageExecutionContext>>();

  public void setModuleRegistry(ModuleRegistry moduleRegistry) {
    this.moduleRegistry = moduleRegistry;
  }

  public Participant getParticipant() {
    Participant template = new Participant();
    template.setFirstName("Michel");
    template.setLastName("Tremblay");
    template.setGender(Gender.MALE);
    template.setBirthDate(new Date(5555555));

    Participant participant = getPersistenceManager().matchOne(template);

    if(participant == null) {
      participant = getPersistenceManager().save(template);
    }

    if(participant.getInterview() == null) {
      Interview interview = new Interview();
      interview.setParticipant(participant);
      interview.setStartDate(new Date());
      getPersistenceManager().save(interview);
      participant = getPersistenceManager().refresh(participant);
    }

    return participant;
  }

  public Interview getInterview() {
    return getParticipant().getInterview();
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
      
      // try to find it in memory
      StageExecutionMemento template = new StageExecutionMemento();
      template.setStage(stage);
      template.setInterview(getInterview());
      StageExecutionMemento memento = getPersistenceManager().matchOne(template);
      // TODO...
      
      if (exec == null) {
        Module module = moduleRegistry.getModule(stage.getModule());
        exec = (StageExecutionContext)module.createStageExecution(getInterview(), stage);
        contexts.put(stage.getId(), exec);
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
  }
  
  public void shutdown() {
    log.info("shutdown");
    // TODO persist stage execution states
  }

}
