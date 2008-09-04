package org.obiba.onyx.webapp.participant.page;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.panel.AssignCodeToParticipantPanel;
import org.obiba.onyx.webapp.participant.panel.EditParticipantPanel;
import org.obiba.onyx.webapp.participant.panel.ParticipantPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class ParticipantReceptionPage extends BasePage {

  public ParticipantReceptionPage(DetachableEntityModel participantModel) {
    super();

    ParticipantPanel participantPanel = new ParticipantPanel("participantPanel", (Participant) participantModel.getObject());
    add(participantPanel);
    add(new AssignCodeToParticipantPanel("assignCodeToParticipantPanel", participantModel));

    //
    // Add the Edit Participant pop-up.
    //
    final ModalWindow editParticipantModalWindow = new ModalWindow("editParticipantModalWindow");
    editParticipantModalWindow.setTitle(new StringResourceModel("Participant", this, null));
    editParticipantModalWindow.setContent(new EditParticipantPanel("content", (Participant)participantModel.getObject(), participantPanel, editParticipantModalWindow));
    add(editParticipantModalWindow);

    @SuppressWarnings("serial")
    AjaxLink link = new AjaxLink("editParticipantAction") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        editParticipantModalWindow.show(target);
      }
    };
    link.add(new Label("editParticipantLabel", new StringResourceModel("Edit", this, null)));

    add(link);
  }
}