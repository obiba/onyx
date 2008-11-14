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
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQuestionCategoryColumn extends AbstractColumn {

  private static final Logger log = LoggerFactory.getLogger(AbstractQuestionCategoryColumn.class);

  private IModel questionCategoryModel;

  public AbstractQuestionCategoryColumn(IModel questionCategoryModel) {
    super(new QuestionnaireStringResourceModel(questionCategoryModel, "label"));
    this.questionCategoryModel = questionCategoryModel;
    // sub classes may need spring beans, do it manually as a column is not a component
    InjectorHolder.getInjector().inject(this);
  }

  public void populateItem(Item cellItem, String componentId, IModel rowModel) {
    cellItem.setModel(questionCategoryModel);
    Question question = (Question) rowModel.getObject();
    final int index = question.getParentQuestion().getQuestions().indexOf(question);
    log.debug("QuestionCategoryColumn.index={}", index);
    populateItem(cellItem, componentId, rowModel, index);
  }

  /**
   * Get the question category model.
   * @return
   */
  public IModel getModel() {
    return questionCategoryModel;
  }

  /**
   * 
   * @param cellItem
   * @param componentId
   * @param rowModel
   * @param index
   */
  public abstract void populateItem(Item cellItem, String componentId, IModel rowModel, int index);

  @Override
  public String getCssClass() {
    return "category";
  }

  /**
   * Called when a ajax refresh is required.
   * @param target
   */
  public void onEvent(AjaxRequestTarget target) {

  }

  /**
   * Called when a ajax refresh is required.
   * @param target
   */
  public void onErrorEvent(AjaxRequestTarget target) {

  }

}