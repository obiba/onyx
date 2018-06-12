package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public abstract class FormMock extends Panel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public FormMock(String id, IModel<?> model) {
    super(id, model);
    Form<?> form = new Form("form");
    add(form);
    if(model.getObject() == null) throw new IllegalArgumentException("Null object in FormMock not accepted.");
    form.add(populateContent("content", model));
  }

  public abstract Component populateContent(String id, IModel<?> model);

}
