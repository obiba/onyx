package org.obiba.onyx.wicket.action;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;

public abstract class ActionDefinitionPanel extends Panel {

  private static final long serialVersionUID = -5173222062528691764L;

  private boolean cancelled = true;

  @SuppressWarnings("serial")
  public ActionDefinitionPanel(String id, ActionDefinition definition) {
    super(id);

    Action action = new Action(definition);
    setModel(new Model(action));

    add(new Label("description", definition.getDescription()));

    Form form = new Form("form");
    add(form);

    form.add(new TextArea("comment", new PropertyModel(this, "action.comment")));

    action.setEventReason(definition.getDefaultReason());
    if(definition.getReasons().size() > 0) {
      form.add(new ReasonsFragment("reasons", definition.getReasons()));
    } else {
      form.add(new Fragment("reasons", "trFragment", this));
    }

    form.add(new AjaxButton("submit", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        cancelled = false;
        ActionDefinitionPanel.this.onClick(target);
      }

    });

    form.add(new AjaxButton("cancel") {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        cancelled = true;
        ActionDefinitionPanel.this.onClick(target);
      }

    });
  }

  public Action getAction() {
    return (Action) getModelObject();
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public abstract void onClick(AjaxRequestTarget target);

  private class ReasonsFragment extends Fragment {

    public ReasonsFragment(String id, List<String> reasons) {
      super(id, "reasonsFragment", ActionDefinitionPanel.this);
      add(new DropDownChoice("reasonsSelect", new PropertyModel(ActionDefinitionPanel.this, "action.eventReason"), reasons));
    }

  }
}
