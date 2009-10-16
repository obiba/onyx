/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.provider;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

/**
 *
 */
public class AllPagesProvider extends AbstractQuestionnaireElementProvider<Page, Section> {

  private static final long serialVersionUID = -2203581882836613910L;

  /**
   * 
   */
  public AllPagesProvider(IModel<Section> model) {
    super(model);
  }

  @Override
  protected List<Page> getElementList() {
    return getProviderElement().getPages();
  }

}
