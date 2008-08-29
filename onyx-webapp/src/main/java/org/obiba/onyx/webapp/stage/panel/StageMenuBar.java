package org.obiba.onyx.webapp.stage.panel;

import java.text.SimpleDateFormat;

import org.apache.wicket.markup.html.basic.Label;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.webapp.base.panel.MenuBar;

public class StageMenuBar extends MenuBar {

  private static final long serialVersionUID = 8805458043658346936L;

  private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");

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
    infoLabel = stage.getName() + ": " + participant.getLastName() + ", " + participant.getFirstName() + "   " + dateFormatter.format(participant.getBirthDate());

    buildMenus();
  }
}