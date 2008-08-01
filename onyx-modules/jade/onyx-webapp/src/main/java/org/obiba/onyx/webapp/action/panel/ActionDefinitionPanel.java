package org.obiba.onyx.webapp.action.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;

public abstract class ActionDefinitionPanel extends Panel {

  private static final long serialVersionUID = -5173222062528691764L;

  private boolean cancelled = true;
  
  public ActionDefinitionPanel(String id, ActionDefinition definition) {
    super(id);
    
    Action action = new Action(definition);
    setModel(new Model(action));
    
    add(new Label("label", definition.getLabel()));
    
    Form form = new Form("form");
    add(form);
    
    form.add(new AjaxButton("submit", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        cancelled = false;
        ActionDefinitionPanel.this.onSubmit(target);
      }
      
    });
    
    form.add(new AjaxButton("cancel") {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        cancelled = true;
      }
      
    });
  }

  public Action getAction() {
    return (Action)getModelObject();
  }
  
  public boolean isCancelled() {
    return cancelled;
  }
  
  public abstract void onSubmit(AjaxRequestTarget target);
}
