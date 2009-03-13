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

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractQuestionCategorySelectionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.QuestionCategoryRadioPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.validation.AnswerCountValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Category column with radio buttons, questions are in the rows.
 */
public class QuestionCategoryRadioColumn extends AbstractQuestionCategoryColumn {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionCategoryRadioColumn.class);

  private IModel radioGroupsModel;

  /**
   * Constructor given a way to find the radio group to associate using row index.
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
    radioGroup.add(new AnswerCountValidator(rowModel));

    AbstractQuestionCategorySelectionPanel qCategoryPanel;
    cellItem.add(qCategoryPanel = new QuestionCategoryRadioPanel(componentId, rowModel, cellItem.getModel(), radioGroup, false));
    // {
    //
    // @Override
    // public void onSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
    // // call for refresh
    // QuestionCategoryRadioColumn.this.onSelection(target, questionModel, questionCategoryModel);
    // }
    //
    // });
    if(qCategoryPanel.hasOpenField()) {
      cellItem.add(new AttributeAppender("class", new Model("category-open"), " "));
    } else {
      cellItem.add(new AttributeAppender("class", new Model("category"), " "));
    }
  }
}
