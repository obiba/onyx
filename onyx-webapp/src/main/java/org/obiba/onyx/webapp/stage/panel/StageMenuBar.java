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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.webapp.base.panel.MenuBar;
import org.obiba.onyx.webapp.participant.panel.ParticipantPanel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;

public class StageMenuBar extends MenuBar {

  private static final long serialVersionUID = 8805458043658346936L;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  private Dialog participantDetailsModalWindow;

  public StageMenuBar(String id, IModel<Stage> stageModel) {
    super(id);

    participantDetailsModalWindow = new Dialog("participantDetailsModalWindow");
    participantDetailsModalWindow.setTitle(new StringResourceModel("Participant", this, null));
    participantDetailsModalWindow.setHeightUnit("em");
    participantDetailsModalWindow.setWidthUnit("em");
    participantDetailsModalWindow.setInitialHeight(45);
    participantDetailsModalWindow.setInitialWidth(34);
    participantDetailsModalWindow.setType(Dialog.Type.PLAIN);
    participantDetailsModalWindow.setOptions(Dialog.Option.CLOSE_OPTION);
    add(participantDetailsModalWindow);

    Participant participant = activeInterviewService.getParticipant();

    add(new Label("stageLabel", new MessageSourceResolvableStringModel(new PropertyModel(stageModel, "description"))));

    // add(new Label("participantLabel", participant.getBarcode()));

    AjaxLink link = new AjaxLink("link") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        ParticipantPanel component = new ParticipantPanel("content", new Model(activeInterviewService.getParticipant()));
        component.add(new AttributeModifier("class", true, new Model("obiba-content participant-panel-content")));
        participantDetailsModalWindow.setContent(component);
        participantDetailsModalWindow.show(target);
      }
    };
    link.add(new Label("label", participant.getBarcode()));
    add(link);
  }

  protected void buildMenus() {
  }
}
