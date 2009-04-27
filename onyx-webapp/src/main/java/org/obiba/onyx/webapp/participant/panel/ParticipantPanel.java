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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Group;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;

public class ParticipantPanel extends Panel {

  @SpringBean
  UserSessionService userSessionService;

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

    if(participant.getRecruitmentType().equals(RecruitmentType.ENROLLED) && !shortList) kvPanel.addRow(new StringResourceModel("EnrollmentId", this, null), new PropertyModel(getModel(), "enrollmentId"));
    if(participant.getBarcode() != null) kvPanel.addRow(new StringResourceModel("ParticipantCode", this, null), new PropertyModel(getModel(), "barcode"));
    kvPanel.addRow(new StringResourceModel("Name", this, null), new PropertyModel(getModel(), "fullName"));

    if(!shortList) {
      kvPanel.addRow(new StringResourceModel("Gender", this, null), new PropertyModel(this, "localizedGender"));
      kvPanel.addRow(new StringResourceModel("BirthDate", this, null), DateModelUtils.getDateModel(new PropertyModel(this, "dateFormat"), new PropertyModel(getModel(), "birthDate")));
      add(new AttributeGroupsFragment("configuredAttributeGroups", getModel()));
    } else {
      add(new EmptyPanel("configuredAttributeGroups"));
    }

    add(kvPanel);
  }

  public String getLocalizedGender() {
    Gender gender = ((Participant) getModelObject()).getGender();
    return (gender != null) ? getString("Gender." + gender) : null;
  }

  public DateFormat getDateFormat() {
    return userSessionService.getDateFormat();
  }

  private class AttributeGroupsFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public AttributeGroupsFragment(String id, IModel participantModel) {
      super(id, "attributeGroupsFragment", ParticipantPanel.this);

      RepeatingView repeater = new RepeatingView("groupRepeater");
      add(repeater);

      List<Group> groups = getGroups();

      for(int i = 0; i < groups.size(); i++) {
        Group group = groups.get(i);

        WebMarkupContainer item = new WebMarkupContainer(repeater.newChildId());
        repeater.add(item);

        String groupNameKey = group.getName();
        String groupName = !group.isDefaultGroup() ? (new SpringStringResourceModel(groupNameKey, groupNameKey)).getString() : null;

        item.add(new Label("groupName", groupName).setVisible(groupName != null));
        item.add(new AttributeGroupFragment("group", participantModel, group));
      }
    }

    private List<Group> getGroups() {
      List<Group> groups = new ArrayList<Group>();

      if(participantMetadata.getConfiguredAttributes().size() != 0) {
        Group currentGroup = null;

        for(ParticipantAttribute attribute : participantMetadata.getConfiguredAttributes()) {
          Group group = attribute.getGroup();

          if((currentGroup == null) || (group.getName() == null && currentGroup.getName() != null) || (group.getName() != null && currentGroup.getName() == null) || (group.getName() != null && currentGroup.getName() != null && !group.getName().equals(currentGroup.getName()))) {
            groups.add(group);
            currentGroup = group;
          }
        }
      }

      return groups;
    }
  }

  private class AttributeGroupFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public AttributeGroupFragment(String id, IModel participantModel, Group group) {
      super(id, "attributeGroupFragment", ParticipantPanel.this);

      RepeatingView repeat = new RepeatingView("attributeRepeater");
      add(repeat);

      Participant participant = (Participant) participantModel.getObject();

      for(final ParticipantAttribute attribute : group.getParticipantAttributes()) {
        WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
        repeat.add(item);
        item.add(new Label("label", new SpringStringResourceModel(new PropertyModel(attribute, "name"))));
        String value = (participant.getConfiguredAttributeValue(attribute.getName()) != null) ? participant.getConfiguredAttributeValue(attribute.getName()).getValueAsString() : null;
        item.add(new Label("field", new Model(value)));
      }
    }
  }
}
