/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractQuestionCategoriesView;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryEscapeFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Escape question categories are a valid way of not answering the normal set of categories (for instance "Prefer not
 * answer" regarding a multiple choice question). The escape categories are presented on the same horizontal line.
 */
public class SimplifiedEscapeQuestionCategoriesPanel extends Panel {

  private static final long serialVersionUID = 5144933183339704600L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(SimplifiedEscapeQuestionCategoriesPanel.class);

  /**
   * Constructor around given question.
   * @param id
   * @param questionModel
   */
  @SuppressWarnings("serial")
  public SimplifiedEscapeQuestionCategoriesPanel(String id, IModel questionModel) {
    this(id, questionModel, null);
  }

  /**
   * Constructor, given a question and a parent question category context.
   * @param id
   * @param questionModel
   * @param parentQuestionCategoryModel
   */
  @SuppressWarnings("serial")
  public SimplifiedEscapeQuestionCategoriesPanel(String id, IModel questionModel, IModel parentQuestionCategoryModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    // escape categories are on the same line by default
    GridView repeater = new AbstractQuestionCategoriesView("category", getModel(), new QuestionCategoryEscapeFilter(true), new QuestionCategoryListToGridPermutator(getModel(), 1)) {

      @Override
      protected void populateItem(Item item) {
        if(item.getModel() == null) {
          item.add(new EmptyPanel("input").setVisible(false));
        } else {
          item.add(new QuestionCategoryLinkPanel("input", item.getModel()));
        }
      }

    };
    add(repeater);
  }

}
