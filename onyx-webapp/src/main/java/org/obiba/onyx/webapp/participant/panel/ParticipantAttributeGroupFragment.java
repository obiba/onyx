package org.obiba.onyx.webapp.participant.panel;

import java.util.List;

import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.core.domain.participant.Group;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.util.data.Data;

public abstract class ParticipantAttributeGroupFragment extends Fragment {

  private static final long serialVersionUID = 1L;

  public ParticipantAttributeGroupFragment(String id, IModel<Participant> participantModel, Group group, Panel parentPanel, List<ParticipantAttribute> attributesToDisplay) {
    super(id, "attributeGroupFragment", parentPanel);

    RepeatingView repeat = new RepeatingView("attributeRepeater");
    add(repeat);

    Participant participant = participantModel.getObject();

    for(final ParticipantAttribute attribute : group.getParticipantAttributes()) {
      if(attributesToDisplay.contains(attribute)) {
        addParticipantAttribute(attribute, repeat, participant);
      }
    }
  }

  public String getAttributeValueAsString(Participant participant, String attributeName) {
    Data attributeValue = getAttributeValue(participant, attributeName);
    if(attributeValue != null) {
      return attributeValue.getValueAsString();
    }
    return null;
  }

  public Data getAttributeValue(Participant participant, String attributeName) {
    Data attributeValue = participant.getEssentialAttributeValue(attributeName);
    if(attributeValue == null) {
      attributeValue = participant.getConfiguredAttributeValue(attributeName);
    }
    return attributeValue;
  }

  abstract protected void
      addParticipantAttribute(ParticipantAttribute attribute, RepeatingView repeat, Participant participant);

}