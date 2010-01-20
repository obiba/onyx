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

import java.util.AbstractList;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractQuestionCategorySelectionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.QuestionCategoryCheckBoxPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.validation.AnswerCountValidator;

/**
 * Category column with check boxes, questions are in the rows.
 */
public class QuestionCategoryCheckBoxColumn extends AbstractQuestionCategoryColumn {

  private static final long serialVersionUID = 1L;

  private IModel<AbstractList<CheckGroup>> checkGroupsModel;

  /**
   * Constructor given a way to find the check box group to associate using row index.
   * @param questionCategoryModel
   * @param defaultQuestionSharedCategoriesPanel
   */
  public QuestionCategoryCheckBoxColumn(IModel questionCategoryModel, IModel<AbstractList<CheckGroup>> checkGroupsModel) {
    super(questionCategoryModel);
    this.checkGroupsModel = checkGroupsModel;
  }

  @SuppressWarnings("serial")
  @Override
  public void populateItem(Item cellItem, String componentId, IModel rowModel, int index) {
    CheckGroup checkGroup = checkGroupsModel.getObject().get(index);
    checkGroup.add(new AnswerCountValidator(rowModel));

    AbstractQuestionCategorySelectionPanel qCategoryPanel;
    cellItem.add(qCategoryPanel = new QuestionCategoryCheckBoxPanel(componentId, rowModel, cellItem.getModel(), checkGroup.getModel(), false));

    if(qCategoryPanel.hasOpenField()) {
      cellItem.add(new AttributeAppender("class", new Model("category category-open"), " "));
    } else {
      cellItem.add(new AttributeAppender("class", new Model("category"), " "));
    }
  }
}