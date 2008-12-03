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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractQuestionCategorySelectionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.QuestionCategoryCheckBoxPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.validation.AnswerCountValidator;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;

public class QuestionCategoryCheckBoxColumn extends AbstractQuestionCategoryColumn {

  private static final long serialVersionUID = 1L;

  private IModel checkGroupsModel;

  /**
   * @param questionCategoryModel
   * @param defaultQuestionSharedCategoriesPanel TODO
   */
  public QuestionCategoryCheckBoxColumn(IModel questionCategoryModel, IModel checkGroupsModel) {
    super(questionCategoryModel);
    this.checkGroupsModel = checkGroupsModel;
  }

  @SuppressWarnings("serial")
  @Override
  public void populateItem(Item cellItem, String componentId, IModel rowModel, int index) {
    CheckGroup checkGroup = ((CheckGroup[]) checkGroupsModel.getObject())[index];
    checkGroup.add(new AnswerCountValidator(rowModel));
    Question question = (Question) rowModel.getObject();
    String label = new QuestionnaireStringResourceModel(question.getParentQuestion(), "label").getString() + " / " + new QuestionnaireStringResourceModel(question, "label").getString();
    checkGroup.setLabel(new Model(label));

    AbstractQuestionCategorySelectionPanel qCategoryPanel;
    cellItem.add(qCategoryPanel = new QuestionCategoryCheckBoxPanel(componentId, rowModel, cellItem.getModel(), checkGroup, false) {
      @Override
      public void onSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
        QuestionCategoryCheckBoxColumn.this.onSelection(target);
      }

      @Override
      public void onOpenFieldSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
        QuestionCategoryCheckBoxColumn.this.onSelection(target);
      }
    });
    if(qCategoryPanel.hasOpenField()) {
      cellItem.add(new AttributeAppender("class", new Model("category-open"), " "));
    } else {
      cellItem.add(new AttributeAppender("class", new Model("category"), " "));
    }
  }

}