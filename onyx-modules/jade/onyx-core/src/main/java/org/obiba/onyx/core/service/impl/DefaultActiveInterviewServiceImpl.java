package org.obiba.onyx.core.service.impl;

import java.util.Date;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultActiveInterviewServiceImpl extends PersistenceManagerAwareService implements ActiveInterviewService {

  private ModuleRegistry moduleRegistry;
  
  public void setModuleRegistry(ModuleRegistry moduleRegistry) {
    this.moduleRegistry = moduleRegistry;
  }

  public Participant getCurrentParticipant() {
    Participant template = new Participant();
    template.setFirstName("Michel");
    template.setLastName("Tremblay");
    
    Participant participant = getPersistenceManager().matchOne(template);
    
    if (participant == null) {
      participant = getPersistenceManager().save(template);
    }
    
    if (participant.getInterview() == null) {
      Interview interview = new Interview();
      interview.setParticipant(participant);
      interview.setStartDate(new Date());
      getPersistenceManager().save(interview);
      participant = getPersistenceManager().refresh(participant);
    }
    
    return participant;
  }

  public IStageExecution getStageExecution(Stage stage) {
    Module module = moduleRegistry.getModule(stage.getModule());
    
    return module.getStageExecution(getCurrentParticipant().getInterview(), stage);
  }

  public void doAction(Stage stage, Action action) {
    action.setInterview(getCurrentParticipant().getInterview());
    action.setStage(stage);
    // TODO persist action
    IStageExecution exec = getStageExecution(stage);
    action.getActionType().act(exec, action);
  }
}
