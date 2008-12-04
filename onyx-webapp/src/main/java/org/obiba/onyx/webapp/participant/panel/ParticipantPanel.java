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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;

public class ParticipantPanel extends Panel {

  @SpringBean
  ParticipantMetadata participantMetadata;

  private static final long serialVersionUID = -5722864134344016349L;

  public ParticipantPanel(String id, IModel participantModel) {
    this(id, participantModel, false);
  }

  public ParticipantPanel(String id, IModel participantModel, boolean shortList) {
    super(id);
    setModel(participantModel);
    setOutputMarkupId(true);

    KeyValueDataPanel kvPanel = new KeyValueDataPanel("participant");

    Participant participant = (Participant) participantModel.getObject();

    if(participant.getRecruitmentType().equals(RecruitmentType.ENROLLED)) kvPanel.addRow(new StringResourceModel("EnrollmentId", this, null), new PropertyModel(getModel(), "enrollmentId"));
    if(participant.getBarcode() != null) kvPanel.addRow(new StringResourceModel("ParticipantCode", this, null), new PropertyModel(getModel(), "barcode"));
    kvPanel.addRow(new StringResourceModel("Name", this, null), new PropertyModel(getModel(), "fullName"));
    kvPanel.addRow(new StringResourceModel("Gender", this, null), new PropertyModel(this, "localizedGender"));
    kvPanel.addRow(new StringResourceModel("BirthDate", this, null), DateModelUtils.getShortDateModel(new PropertyModel(getModel(), "birthDate")));

    for(ParticipantAttribute attribute : participantMetadata.getConfiguredAttributes()) {
      String value = (participant.getConfiguredAttributeValue(attribute.getName()) != null) ? participant.getConfiguredAttributeValue(attribute.getName()).getValueAsString() : null;
      kvPanel.addRow(new SpringStringResourceModel(new PropertyModel(attribute, "name")), new Model(value));
    }

    add(kvPanel);
  }

  public String getLocalizedGender() {
    return getString("Gender." + ((Participant) getModelObject()).getGender());
  }
}
