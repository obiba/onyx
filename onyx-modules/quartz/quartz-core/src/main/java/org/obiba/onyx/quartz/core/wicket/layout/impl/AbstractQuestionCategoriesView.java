/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.IDataListFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.IDataListPermutator;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoriesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for viewing the question categories in a grid, given a category list and a permutation rule.
 * @see IDataListPermutator
 * @see QuestionCategoriesProvider
 */
public abstract class AbstractQuestionCategoriesView extends GridView {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractQuestionCategoriesView.class);

  /**
   * A map for creating row items at a given index only once.
   */
  private Map<Integer, Item> rowIndexToItem = new HashMap<Integer, Item>();

  /**
   * Constructor using a {@link QuestionCategoriesProvider} as a data provider for the grid.
   * @param id
   * @param questionModel the question from which categories are extracted
   * @param filter filters the categories list
   * @param permutator permute the categories of the list so that it can be presented in a grid
   */
  public AbstractQuestionCategoriesView(String id, IModel questionModel, IDataListFilter<QuestionCategory> filter, IDataListPermutator<QuestionCategory> permutator) {
    super(id, new QuestionCategoriesProvider(questionModel, filter, permutator));

    QuestionCategoriesProvider provider = ((QuestionCategoriesProvider) getDataProvider());
    int count = provider.getDataListPermutator().getColumnCount();
    if(count >= 1) {
      setColumns(count);
    }

    setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
  }

  @Override
  protected void populateEmptyItem(Item item) {
    item.add(new EmptyPanel("input").setVisible(false));
  }

  /**
   * Create a new row or reuse a previously instanciated one at the same index.
   */
  @Override
  protected Item newRowItem(String id, int index) {
    // see QUA-79
    Item rowItem = rowIndexToItem.get(index);
    if(rowItem == null) {
      rowItem = new OddEvenItem(id, index, null);
      rowIndexToItem.put(index, rowItem);
    } else {
      // make sure we can add children to this reused item
      rowItem.removeAll();
    }
    return rowItem;
  }

}
