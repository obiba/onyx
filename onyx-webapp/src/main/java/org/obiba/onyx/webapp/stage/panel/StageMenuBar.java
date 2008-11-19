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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.webapp.base.panel.MenuBar;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;

public class StageMenuBar extends MenuBar {

  private static final long serialVersionUID = 8805458043658346936L;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  public StageMenuBar(String id, IModel stageModel) {
    super(id);
    setOutputMarkupId(true);

    Participant participant = activeInterviewService.getParticipant();

    add(new Label("stageLabel", new MessageSourceResolvableStringModel(new PropertyModel(stageModel, "description"))));
    add(new Label("participantLabel", participant.getFullName() + " | " + participant.getBarcode()));
    add(new Label("birthDateLabel", DateModelUtils.getDateModel(new Model(participant.getBirthDate()))));
  }

  protected void buildMenus() {
  }
}
