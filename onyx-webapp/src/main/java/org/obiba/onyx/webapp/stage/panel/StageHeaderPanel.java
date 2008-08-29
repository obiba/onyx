package org.obiba.onyx.webapp.stage.panel;

import org.obiba.onyx.webapp.base.panel.HeaderPanel;

public class StageHeaderPanel extends HeaderPanel {
  
  private static final long serialVersionUID = 7480791934966023398L;

  public StageHeaderPanel(String id) {
    super(id);
    
    remove("quit");
  }
}
