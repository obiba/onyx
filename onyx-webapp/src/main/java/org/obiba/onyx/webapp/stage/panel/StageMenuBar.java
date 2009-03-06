/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.stage.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.webapp.base.panel.MenuBar;
import org.obiba.onyx.webapp.participant.panel.ParticipantModalPanel;
import org.obiba.onyx.webapp.participant.panel.ParticipantPanel;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;

public class StageMenuBar extends MenuBar {

  private static final long serialVersionUID = 8805458043658346936L;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  private ModalWindow participantDetailsModalWindow;

  public StageMenuBar(String id, IModel stageModel) {
    super(id);
    setOutputMarkupId(true);

    participantDetailsModalWindow = new ModalWindow("participantDetailsModalWindow");
    participantDetailsModalWindow.setCssClassName("onyx");
    participantDetailsModalWindow.setTitle(new StringResourceModel("Participant", this, null));
    participantDetailsModalWindow.setInitialHeight(300);
    participantDetailsModalWindow.setInitialWidth(400);
    add(participantDetailsModalWindow);

    Participant participant = activeInterviewService.getParticipant();

    add(new Label("stageLabel", new MessageSourceResolvableStringModel(new PropertyModel(stageModel, "description"))));

    // add(new Label("participantLabel", participant.getBarcode()));

    AjaxLink link = new AjaxLink("link") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        participantDetailsModalWindow.setContent(new ParticipantModalPanel("content", new ParticipantPanel(ParticipantModalPanel.CONTENT_PANEL_ID, new Model(activeInterviewService.getParticipant())), participantDetailsModalWindow));
        participantDetailsModalWindow.show(target);
      }
    };
    link.add(new Label("label", participant.getBarcode()));
    add(link);
  }

  protected void buildMenus() {
  }
}
