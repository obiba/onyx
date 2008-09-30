package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;
import org.obiba.onyx.jade.core.wicket.run.InstrumentRunPanel;

public class ConclusionPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  @SuppressWarnings("serial")
  public ConclusionPanel(String id) {
    super(id);
    setOutputMarkupId(true);
    
    final ModalWindow modal;
    add(modal = new ModalWindow("modal"));
    modal.setCookieName("instrument-run-modal");
    
    add(new AjaxLink("show") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        modal.setContent(new InstrumentRunPanel(modal.getContentId()));
        modal.show(target);
      }
      
    });
  }

}
