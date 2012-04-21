/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.IDataListFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.IDataListPermutator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a question (and therefore its categories) and a rule to filter the categories, place there corresponding
 * components in a grid.
 */
public abstract class QuestionCategoryComponentsView extends Panel {

  private static final long serialVersionUID = 5144933183339704600L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionCategoryComponentsView.class);

  private GridView repeater;

  /**
   * Constructor around given question.
   * @param id
   * @param questionModel
   */
  @SuppressWarnings("serial")
  public QuestionCategoryComponentsView(String id, IModel<Question> questionModel, IDataListFilter<QuestionCategory> filter, IDataListPermutator<IModel<QuestionCategory>> permutator) {
    super(id, questionModel);
    setOutputMarkupId(true);

    // escape categories are on the same line by default
    repeater = new AbstractQuestionCategoriesView("category", questionModel, filter, permutator) {

      @Override
      protected void populateItem(Item<QuestionCategory> item) {
        if(item.getModel() == null) {
          item.add(new EmptyPanel("input").setVisible(false));
        } else {
          item.add(newQuestionCategoryComponent("input", item.getModel(), item.getIndex()));
        }
      }

    };
    add(repeater);

    add(new QuestionCategoryComponentsViewStyleBehavior());
  }

  /**
   * Get the number of columns.
   * @return
   */
  public int getColumns() {
    return repeater.getColumns();
  }

  /**
   * Builds the component for given question category.
   * @param id
   * @param questionCategoryModel
   * @return
   */
  protected abstract Component
      newQuestionCategoryComponent(String id, IModel<QuestionCategory> questionCategoryModel, int index);

  /**
   * Set the appropriate css class to custom grid view according to columns count.
   */
  private class QuestionCategoryComponentsViewStyleBehavior extends AbstractBehavior {

    private static final long serialVersionUID = 1L;

    private static final String GRID_CLASS_PREFIX = "obiba-quartz-grid";

    public QuestionCategoryComponentsViewStyleBehavior() {
      super();
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
      super.onComponentTag(component, tag);

      QuestionCategoryComponentsView view = (QuestionCategoryComponentsView) component;

      String cssClass = GRID_CLASS_PREFIX + "-" + view.getColumns();
      if(tag.getAttributes().containsKey("class")) {
        cssClass += " " + tag.getAttributes().getString("class");
      }
      tag.getAttributes().put("class", cssClass);

    }
  }

}
