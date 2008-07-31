package org.obiba.onyx.webapp.home.page;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.stage.panel.StageSelectionPanel;

public class HomePage extends BasePage {

  @SpringBean
  private ModuleRegistry registry;
  
  @SpringBean(name="participantService")
  private ParticipantService participantService;
  
  public HomePage() {
    super();
    
    Participant participant = participantService.getCurrentParticipant();
    add(new Label("participant", participant.getFirstName() + " " + participant.getLastName()));
    
    add(new StageSelectionPanel("stage-list", getFeedbackPanel()));
  }

}
