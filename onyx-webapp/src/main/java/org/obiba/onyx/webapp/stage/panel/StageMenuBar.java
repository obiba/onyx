package org.obiba.onyx.webapp.stage.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.webapp.base.panel.MenuBar;
import org.obiba.onyx.wicket.util.DateModelUtils;

public class StageMenuBar extends MenuBar {

  private static final long serialVersionUID = 8805458043658346936L;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  public StageMenuBar(String id, IModel stageModel) {
    super(id);
    setOutputMarkupId(true);

    Participant participant = activeInterviewService.getParticipant();

    add(new Label("stageLabel", new PropertyModel(stageModel, "description")));
    add(new Label("participantLabel", participant.getFullName() + " | " + participant.getBarcode()));
    add(new Label("birthDateLabel", DateModelUtils.getDateModel(new PropertyModel(participant, "birthDate"))));
  }

  protected void buildMenus() {
  }
}