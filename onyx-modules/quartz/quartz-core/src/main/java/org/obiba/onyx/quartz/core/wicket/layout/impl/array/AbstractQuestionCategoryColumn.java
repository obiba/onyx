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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
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

  public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
    cellItem.setModel(questionCategoryModel);
    // find the row item index by exploring parents.
    cellItem.visitParents(Item.class, new Component.IVisitor() {

      public Object component(Component component) {
        if(!component.equals(cellItem)) {
          Item item = (Item) component;
          log.debug("found item.index={}", item.getIndex());
          populateItem(cellItem, componentId, rowModel, item.getIndex());
        }
        return null;
      }
    });
  }

  @Override
  public Component getHeader(String componentId) {
    return super.getHeader(componentId).setEscapeModelStrings(false);
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
  public void onSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {

  }

}