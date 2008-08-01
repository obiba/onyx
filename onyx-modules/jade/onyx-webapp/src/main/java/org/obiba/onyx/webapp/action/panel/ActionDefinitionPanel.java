package org.obiba.onyx.webapp.action.panel;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;

public class ActionDefinitionPanel extends Panel {

  private static final long serialVersionUID = -5173222062528691764L;

  public ActionDefinitionPanel(String id, ActionDefinition definition) {
    super(id);
    Action action = new Action();
    action.setActionType(definition.getType());
    setModel(new Model(action));
    
  }

  public Action getAction() {
    return (Action)getModelObject();
  }
  
}
