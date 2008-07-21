package org.obiba.onyx.jade.core.wicket.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public class JadePanel extends Panel {

  private static final long serialVersionUID = -6692482689347742363L;

  @SpringBean
  private EntityQueryService queryService;
  
  public JadePanel(String id, InstrumentType type) {
    super(id);
    setModel(new DetachableEntityModel(queryService, type));
    
    add(new Label("description", type.getDescription()));
    
    add(new InstrumentLauncherPanel("launcher", getModel()));
    
    add(new InstrumentPanel("content", new Model("instrument")));
  }

}
