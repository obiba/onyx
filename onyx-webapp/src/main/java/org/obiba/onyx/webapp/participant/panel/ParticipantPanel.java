package org.obiba.onyx.webapp.participant.panel;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.wicket.util.DateUtils;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public class ParticipantPanel extends Panel {

  private static final long serialVersionUID = 3349313197922143705L;

  @SpringBean
  private EntityQueryService queryService;

  public ParticipantPanel(String id, Participant participant) {
    this(id, participant, false);
  }

  public ParticipantPanel(String id, Participant participant, boolean shortList) {
    super(id);
    setModel(new DetachableEntityModel(queryService, participant));

    KeyValueDataPanel kvPanel = new KeyValueDataPanel("participant");

    kvPanel.addRow(new StringResourceModel("ParticipantCode", this, null), new PropertyModel(participant, "barcode"));
    kvPanel.addRow(new StringResourceModel("AppointmentCode", this, null), new PropertyModel(participant, "appointment.appointmentCode"));
    kvPanel.addRow(new StringResourceModel("Name", this, null), new PropertyModel(participant, "fullName"));
    kvPanel.addRow(new StringResourceModel("Gender", this, null), new StringResourceModel("Gender." + participant.getGender(), this, null));
    kvPanel.addRow(new StringResourceModel("BirthDate", this, null), DateUtils.getDateModel(new PropertyModel(participant, "birthDate")));

    if(!shortList) {
      kvPanel.addRow(new StringResourceModel("Street", this, null), new PropertyModel(participant, "street"));
      kvPanel.addRow(new StringResourceModel("Apartment", this, null), new PropertyModel(participant, "apartment"));
      kvPanel.addRow(new StringResourceModel("City", this, null), new PropertyModel(participant, "city"));
      kvPanel.addRow(new StringResourceModel("Province", this, null), new PropertyModel(participant, "province"));
      kvPanel.addRow(new StringResourceModel("Country", this, null), new PropertyModel(participant, "country"));
      kvPanel.addRow(new StringResourceModel("PostalCode", this, null), new PropertyModel(participant, "postalCode"));
      kvPanel.addRow(new StringResourceModel("Phone", this, null), new PropertyModel(participant, "phone"));
    }

    add(kvPanel);
  }

}
