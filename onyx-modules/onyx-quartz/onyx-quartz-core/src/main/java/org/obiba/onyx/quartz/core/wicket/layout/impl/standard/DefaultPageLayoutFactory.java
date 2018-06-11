/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.wicket.layout.IPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayout;

/**
 * Default page panel factory.
 * @see DefaultPageLayout
 */
public class DefaultPageLayoutFactory implements IPageLayoutFactory {

  @Override
  public PageLayout createLayout(String id, IModel<Page> pageModel) {
    return new DefaultPageLayout(id, pageModel);
  }

  @Override
  public String getBeanName() {
    return "quartz." + getClass().getSimpleName();
  }

}
