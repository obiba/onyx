package org.obiba.onyx.webapp.page.home;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.StageExecution;
import org.obiba.onyx.engine.StageExecutionStatus;
import org.obiba.onyx.webapp.page.base.BasePage;
import org.obiba.onyx.webapp.panel.stage.StageSelectionPanel;

public class HomePage extends BasePage {

  @SpringBean
  private ModuleRegistry registry;
  
  @SpringBean(name="participantService")
  private ParticipantService participantService;
  
  public HomePage() {
    super();
    
    for (Module module : registry.getModules()) {
      StageExecution exec = module.getCurrentStageExecution(); 
      if (!exec.getStatus().equals(StageExecutionStatus.READY)) {
        info(exec.getStage() + " is " + exec.getStatus());
      }
    }
    
    Participant participant = participantService.getParticipant();
    add(new Label("participant", participant.getFirstName() + " " + participant.getLastName()));
    
    add(new StageSelectionPanel("stage-list", getFeedbackPanel()));
  }

}
