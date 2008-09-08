package org.obiba.onyx.webapp.stage.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.webapp.base.panel.MenuBar;
import org.obiba.onyx.wicket.util.DateUtils;

public class StageMenuBar extends MenuBar {

  private static final long serialVersionUID = 8805458043658346936L;

  private String infoLabel;

  public StageMenuBar(String id) {
    super(id);
  }

  protected void buildMenus() {
    if(infoLabel != null) {
      add(new Label("infoLabel", infoLabel));
    }
  }

  public void setInfoLabel(Stage stage, Participant participant) {
    infoLabel = stage.getDescription() + ": " + participant.getFullName() + " - "  + participant.getBarcode() + " - " + DateUtils.getDateModel(new Model(participant.getBirthDate())).getObject().toString();

    buildMenus();
  }
}