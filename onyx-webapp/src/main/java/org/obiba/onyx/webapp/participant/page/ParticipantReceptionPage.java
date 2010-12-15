/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.page;

import org.apache.wicket.Page;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.panel.EditParticipantFormPanel;

@AuthorizeInstantiation({ "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class ParticipantReceptionPage extends BasePage {

  public ParticipantReceptionPage(IModel participantModel, Page sourcePage) {
    super();

    Participant participant = (Participant) participantModel.getObject();

    switch(participant.getRecruitmentType()) {
    case ENROLLED:
      add(new TitleFragment("title", "participantReception"));
      break;
    case VOLUNTEER:
      add(new TitleFragment("title", "volunteerRegistration"));
      break;
    }

    add(new EditParticipantFormPanel("editParticipantFormPanel", participantModel, sourcePage));
  }

  @SuppressWarnings("serial")
  private class TitleFragment extends Fragment {

    public TitleFragment(String id, String titleKey) {
      super(id, "titleFragment", ParticipantReceptionPage.this);
      add(new Label("titleMessage", new ResourceModel(titleKey)));
    }
  }
}
