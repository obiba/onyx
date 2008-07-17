package org.obiba.onyx.jade.core.wicket.panel;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class JadePanel extends Panel {

  private static final long serialVersionUID = -6692482689347742363L;

  public JadePanel(String id) {
    super(id);
    add(new InstrumentTypeSelectionPanel("content"));
    add(new InstrumentPanel("wizard", new Model("instrument")));
  }

}
