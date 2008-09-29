package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.markup.html.panel.Panel;
import org.obiba.onyx.jade.core.wicket.run.InstrumentRunPanel;

public class ConclusionPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  public ConclusionPanel(String id) {
    super(id);
    setOutputMarkupId(true);
    
    add(new InstrumentRunPanel("run"));
  }

}
