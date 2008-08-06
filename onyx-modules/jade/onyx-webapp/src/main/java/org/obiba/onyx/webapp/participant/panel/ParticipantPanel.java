package org.obiba.onyx.webapp.participant.panel;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.webapp.util.DateUtils;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public class ParticipantPanel extends Panel {
  
  private static final long serialVersionUID = 3349313197922143705L;
  
  @SpringBean
  private EntityQueryService queryService;
  
  public ParticipantPanel(String id, Participant participant) {
    super(id);
    setModel(new DetachableEntityModel(queryService, participant));
    
    KeyValueDataPanel kvPanel = new KeyValueDataPanel("participant");
    kvPanel.addRow(new StringResourceModel("FirstName", this, null), new PropertyModel(participant, "firstName"));
    kvPanel.addRow(new StringResourceModel("LastName", this, null), new PropertyModel(participant, "lastName"));
    kvPanel.addRow(new StringResourceModel("Gender", this, null), new StringResourceModel("Gender." + participant.getGender(), this, null));
    kvPanel.addRow(new StringResourceModel("BirthDate", this, null), DateUtils.getDateModel(new PropertyModel(participant, "birthDate")));
    add(kvPanel);
  }
  
}
