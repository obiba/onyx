package org.obiba.onyx.webapp.participant.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

public class ParticipantModalPanel extends Panel {

  public ParticipantModalPanel(String id, Panel contentPanel, final ModalWindow modalWindow) {
    super(id);
    
    add(contentPanel);
    
    AjaxLink link = new AjaxLink("closeAction") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        modalWindow.close(target);
      }
    };
    link.add(new Label("closeActionLabel", new StringResourceModel("Close", this, null)));
    
    add(link);
  }
}
