package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanel;

@SuppressWarnings("serial")
public class DropDownQuestionPanelMock extends FormMock {

  public DropDownQuestionPanelMock(String id, IModel model) {
    super(id, model);
  }

  @Override
  public Component populateContent(String id, IModel model) {
    return new DropDownQuestionPanel(id, model);
  }

}
