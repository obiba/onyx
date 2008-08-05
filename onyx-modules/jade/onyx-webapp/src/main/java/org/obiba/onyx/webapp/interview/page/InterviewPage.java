package org.obiba.onyx.webapp.interview.page;

import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.stage.panel.StageSelectionPanel;
import org.obiba.onyx.webapp.util.DateUtils;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;

public class InterviewPage extends BasePage {

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  public InterviewPage() {
    super();

    Participant participant = activeInterviewService.getParticipant();
    KeyValueDataPanel kvPanel = new KeyValueDataPanel("participant");
    kvPanel.addRow(new StringResourceModel("FirstName", this, null), new PropertyModel(participant, "firstName"));
    kvPanel.addRow(new StringResourceModel("LastName", this, null), new PropertyModel(participant, "lastName"));
    kvPanel.addRow(new StringResourceModel("Gender", this, null), new StringResourceModel("Gender." + participant.getGender(), this, null));
    kvPanel.addRow(new StringResourceModel("BirthDate", this, null), DateUtils.getDateModel(new PropertyModel(participant, "birthDate")));
    add(kvPanel);

    Interview interview = activeInterviewService.getInterview();
    kvPanel = new KeyValueDataPanel("interview");
    kvPanel.addRow(new StringResourceModel("StartDate", this, null), DateUtils.getDateModel(new PropertyModel(interview, "startDate")));
    kvPanel.addRow(new StringResourceModel("EndDate", this, null), DateUtils.getDateModel(new PropertyModel(interview, "stopDate")));
    kvPanel.addRow(new StringResourceModel("Status", this, null), new StringResourceModel("InterviewStatus." + interview.getStatus(), this, null));
    add(kvPanel);

    add(new StageSelectionPanel("stage-list", getFeedbackPanel()));
  }

  

}
