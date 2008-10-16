package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.wicket.layout.IPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayout;

public class DefaultPageLayoutFactory implements IPageLayoutFactory {

  public PageLayout createLayout(String id, Page page) {
    return new DefaultPageLayout(id, new Model(page));
  }

  public String getName() {
    return "quartz." + getClass().getSimpleName();
  }

}
