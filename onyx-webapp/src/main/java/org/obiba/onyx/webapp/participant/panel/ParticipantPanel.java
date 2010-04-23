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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Group;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;

public class ParticipantPanel extends Panel {

  @SpringBean(name = "userSessionService")
  UserSessionService userSessionService;

  @SpringBean
  ParticipantMetadata participantMetadata;

  private static final long serialVersionUID = -5722864134344016349L;

  public ParticipantPanel(String id, IModel<Participant> participantModel) {
    this(id, participantModel, false);
  }

  public ParticipantPanel(String id, IModel<Participant> participantModel, boolean shortList) {
    super(id, participantModel);

    setOutputMarkupId(true);

    Participant participant = (Participant) participantModel.getObject();

    add(new ParticipantPanelAttributeGroupsFragment("essentialAttributeGroup", getDefaultModel(), getEssentialAttributesToDisplay(participant, shortList), participantMetadata, this));
    if(!shortList) {
      add(new ParticipantPanelAttributeGroupsFragment("configuredAttributeGroups", getDefaultModel(), participantMetadata.getConfiguredAttributes(), participantMetadata, this));
    } else {
      add(new EmptyPanel("configuredAttributeGroups"));
    }
  }

  public List<ParticipantAttribute> getEssentialAttributesToDisplay(Participant participant, boolean shortList) {
    List<ParticipantAttribute> attributesToDisplay = new ArrayList<ParticipantAttribute>();

    if(participant.getRecruitmentType().equals(RecruitmentType.ENROLLED) && !shortList) attributesToDisplay.add(participantMetadata.getEssentialAttribute(ParticipantMetadata.ENROLLMENT_ID_ATTRIBUTE_NAME));
    if(participant.getBarcode() != null) attributesToDisplay.add(participantMetadata.getEssentialAttribute(ParticipantMetadata.PARTICIPANT_ID));
    attributesToDisplay.add(participantMetadata.getEssentialAttribute(ParticipantMetadata.LAST_NAME_ATTRIBUTE_NAME));
    attributesToDisplay.add(participantMetadata.getEssentialAttribute(ParticipantMetadata.FIRST_NAME_ATTRIBUTE_NAME));

    if(!shortList) {
      attributesToDisplay.add(participantMetadata.getEssentialAttribute(ParticipantMetadata.GENDER_ATTRIBUTE_NAME));
      attributesToDisplay.add(participantMetadata.getEssentialAttribute(ParticipantMetadata.BIRTH_DATE_ATTRIBUTE_NAME));
    }

    return attributesToDisplay;
  }

  public String getLocalizedGender() {
    Gender gender = ((Participant) getDefaultModelObject()).getGender();
    return (gender != null) ? getString("Gender." + gender) : null;
  }

  public DateFormat getDateFormat() {
    return userSessionService.getDateFormat();
  }

  private class ParticipantPanelAttributeGroupsFragment extends ParticipantAttributeGroupsFragment {

    protected ParticipantPanelAttributeGroupsFragment(String id, IModel participantModel, List<ParticipantAttribute> attributes, ParticipantMetadata participantMetadata, Panel parentPanel) {
      super(id, participantModel, attributes, participantMetadata, parentPanel);
    }

    @Override
    protected ParticipantAttributeGroupFragment addAttributeGroupFragment(String id, IModel<Participant> participantModel, Group group, Panel parentPanel, List<ParticipantAttribute> attributes) {
      return new ParticipantPanelAttributeGroupFragment(id, participantModel, group, attributes);
    }

  }

  private class ParticipantPanelAttributeGroupFragment extends ParticipantAttributeGroupFragment {

    private static final long serialVersionUID = 1L;

    private List<ParticipantAttribute> attributesToDisplay;

    public ParticipantPanelAttributeGroupFragment(String id, IModel participantModel, Group group, List<ParticipantAttribute> attributes) {
      super(id, participantModel, group, ParticipantPanel.this, attributes);
    }

    @Override
    protected void addParticipantAttribute(ParticipantAttribute attribute, RepeatingView repeat, Participant participant) {
      WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
      repeat.add(item);
      item.add(new Label("label", new SpringStringResourceModel(new PropertyModel(attribute, "name"))));
      String value = getAttributeValueAsString(participant, attribute.getName());
      item.add(new Label("field", new Model(value)));
    }

  }

}
