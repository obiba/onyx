/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.RecruitmentType;

public class ParticipantRegistryPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private ParticipantRegistrySearchPanel participantRegistrySearchPanel;

  private ParticipantPanel participantPanel;

  public ParticipantRegistryPanel(String id) {
    this(id, emptyParticipantModel());
  }

  public ParticipantRegistryPanel(String id, IModel<Participant> model) {
    super(id, model);
    setOutputMarkupId(true);
    add(new AttributeModifier("class", true, new Model<String>("participant-registry-search-panel")));
    participantRegistrySearchPanel = new ParticipantRegistrySearchPanel("participant-registry-search-panel");
    add(participantRegistrySearchPanel);
    participantPanel = new ParticipantPanel("participant-view-panel", model);
    add(participantPanel);
  }

  private static IModel<Participant> emptyParticipantModel() {
    Participant emptyParticipant = new Participant();
    emptyParticipant.setRecruitmentType(RecruitmentType.VOLUNTEER);
    emptyParticipant.setFirstName("");
    emptyParticipant.setLastName("");
    return new Model<Participant>(emptyParticipant);
  }

  public Participant lookUpParticipant() {
    Participant p = participantRegistrySearchPanel.lookupParticipant();
    participantPanel = new ParticipantPanel("participant-view-panel", new Model<Participant>(p));
    addOrReplace(participantPanel);
    return null;
  }

  public void setMessage(String string) {
    participantRegistrySearchPanel.setMessage(string);
  }

  public void clearMessage() {
    participantRegistrySearchPanel.clearMessage();
  }

  public void reset() {
    resetResults();
    participantRegistrySearchPanel.reset();
  }

  public void resetResults() {
    participantPanel = new ParticipantPanel("participant-view-panel", emptyParticipantModel());
    addOrReplace(participantPanel);
  }
}
