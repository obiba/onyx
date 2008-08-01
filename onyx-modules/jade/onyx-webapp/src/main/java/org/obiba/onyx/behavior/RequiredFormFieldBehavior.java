package org.obiba.onyx.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;

public class RequiredFormFieldBehavior extends AttributeAppender {

  private static final long serialVersionUID = -547194106510259211L;

  public RequiredFormFieldBehavior() {
    super("class", new Model("required"), " ");
  }

  @Override
  public void bind(Component component) {
    FormComponent formComp = (FormComponent) component;
    formComp.setRequired(true);
    super.bind(component);
  }

};
