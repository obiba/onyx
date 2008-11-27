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
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.panel.EditParticipantPanel;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class ParticipantReceptionPage extends BasePage {

  private static final String RECEPTION = "reception";

  private static final String ENROLLMENT = "enrollment";

  @SuppressWarnings("serial")
  public ParticipantReceptionPage(IModel participantModel, Page sourcePage, String mode) {
    super();

    if(mode.equals(RECEPTION)) {
      add(new TitleFragment("title", "participantReception"));
      add(new InstructionFragment("instruction", "receiveParticipantInstructions"));
    } else if(mode.equals(ENROLLMENT)) {
      add(new TitleFragment("title", "volunteerRegistration"));
      add(new InstructionFragment("instruction", "enrollParticipantInstructions"));
    }

    add(new EditParticipantPanel("editParticipantPanel", participantModel, sourcePage, mode));
  }

  @SuppressWarnings("serial")
  private class TitleFragment extends Fragment {

    public TitleFragment(String id, String titleKey) {
      super(id, "titleFragment", ParticipantReceptionPage.this);
      add(new Label("titleMessage", new ResourceModel(titleKey)));
    }
  }

  @SuppressWarnings("serial")
  private class InstructionFragment extends Fragment {

    public InstructionFragment(String id, String messageKey) {
      super(id, "instructionFragment", ParticipantReceptionPage.this);
      add(new MultiLineLabel("instructionMessage", new ResourceModel(messageKey)));
    }
  }
}
