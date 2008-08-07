package org.obiba.onyx.webapp.participant.page;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.panel.AssignCodeToParticipantPanel;
import org.obiba.onyx.webapp.participant.panel.ParticipantPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public class ParticipantReceptionPage extends BasePage {

  public ParticipantReceptionPage(DetachableEntityModel participant) {
    super();

    add(new ParticipantPanel("participantPanel", (Participant) participant.getObject()));
    add(new AssignCodeToParticipantPanel("assignCodeToParticipantPanel", participant));
  }

}
