package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultQuestionPanel;

@SuppressWarnings("serial")
public class DefaultQuestionPanelMock extends FormMock {

  public DefaultQuestionPanelMock(String id, IModel model) {
    super(id, model);
  }

  @Override
  public Component populateContent(String id, IModel model) {
    return new DefaultQuestionPanel(id, model);
  }

}
