package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public abstract class FormMock extends Panel {

  public FormMock(String id, IModel model) {
    super(id, model);
    Form form = new Form("form");
    add(form);
    form.add(populateContent("content", getModel()));
  }

  public abstract Component populateContent(String id, IModel model);

}
