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
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.impl.QuestionCategoryRadioPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class QuestionCategoryRadioColumn extends AbstractQuestionCategoryColumn {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionCategoryRadioColumn.class);

  private IModel radioGroupsModel;

  /**
   * 
   * @param questionCategoryModel
   * @param radioGroupsModel
   */
  public QuestionCategoryRadioColumn(IModel questionCategoryModel, IModel radioGroupsModel) {
    super(questionCategoryModel);
    this.radioGroupsModel = radioGroupsModel;
  }

  @SuppressWarnings("serial")
  @Override
  public void populateItem(Item cellItem, String componentId, IModel rowModel, final int index) {
    RadioGroup radioGroup = ((RadioGroup[]) radioGroupsModel.getObject())[index];
    radioGroup.setRequired(((Question) rowModel.getObject()).isRequired());
    radioGroup.setLabel(new QuestionnaireStringResourceModel(rowModel, "label"));

    cellItem.add(new QuestionCategoryRadioPanel(componentId, rowModel, cellItem.getModel(), radioGroup, false) {

      @Override
      public void onOpenFieldSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
        // call for refresh
        onEvent(target);
      }

      @Override
      public void onOpenFieldSubmit(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
        // call for refresh
        onEvent(target);
      }

      @Override
      public void onOpenFieldError(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
        // call for refresh
        onErrorEvent(target);
      }

      @Override
      public void onSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
        // call for refresh
        onEvent(target);
      }

    });
  }
}
