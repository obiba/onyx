/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.array;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultQuestionCategoriesPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;

/**
 * Each cell contains the question categories UI, in the context of one of the parent question categories.
 */
public class QuestionCategoriesColumn extends AbstractColumn {

  private static final long serialVersionUID = 1L;

  private IModel questionModel;

  /**
   * @param displayModel
   */
  public QuestionCategoriesColumn(IModel questionModel) {
    super(new QuestionnaireStringResourceModel(questionModel, "label"));
    this.questionModel = questionModel;
  }

  public void populateItem(Item cellItem, String componentId, IModel parentQuestionCategoryModel) {
    cellItem.setModel(questionModel);

    cellItem.add(new DefaultQuestionCategoriesPanel(componentId, questionModel, parentQuestionCategoryModel));
  }

  public IModel getModel() {
    return questionModel;
  }

}
