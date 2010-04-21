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

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.core.domain.participant.Group;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;

abstract public class ParticipantAttributeGroupsFragment extends Fragment {

  private static final long serialVersionUID = 1L;

  protected ParticipantAttributeGroupsFragment(String id, IModel<Participant> participantModel, List<ParticipantAttribute> attributes, ParticipantMetadata participantMetadata, Panel parentPanel) {
    super(id, "attributeGroupsFragment", parentPanel);

    RepeatingView repeater = new RepeatingView("groupRepeater");
    add(repeater);

    List<Group> groups = participantMetadata.getGroups(attributes);

    for(int i = 0; i < groups.size(); i++) {
      Group group = groups.get(i);

      WebMarkupContainer item = new WebMarkupContainer(repeater.newChildId());
      repeater.add(item);

      String groupNameKey = group.getName();
      String groupName = !group.isDefaultGroup() ? (new SpringStringResourceModel(groupNameKey, groupNameKey)).getString() : null;

      item.add(new Label("groupName", groupName).setVisible(groupName != null));
      item.add(newAttributeGroupFragment("group", participantModel, group, parentPanel, attributes));
    }
  }

  abstract protected ParticipantAttributeGroupFragment newAttributeGroupFragment(String id, IModel<Participant> participantModel, Group group, Panel parentPanel, List<ParticipantAttribute> attributes);

}
