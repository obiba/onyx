package org.obiba.onyx.webapp.page.home;

import org.obiba.onyx.webapp.page.base.BasePage;
import org.obiba.onyx.webapp.panel.stage.StageSelectionPanel;

public class HomePage extends BasePage {

  public HomePage() {
    super();
    add(new StageSelectionPanel("stage-list"));
  }

}
