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

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoriesProvider;

/**
 * 
 */
public abstract class AbstractQuestionCategoriesView extends GridView {

  public AbstractQuestionCategoriesView(String id, IModel questionModel) {
    this(id, questionModel, null);
  }

  public AbstractQuestionCategoriesView(String id, IModel questionModel, QuestionCategoriesProvider.IQuestionCategoryFilter filter) {
    super(id, new QuestionCategoriesProvider(questionModel, filter));
    setColumns(((QuestionCategoriesProvider) getDataProvider()).getPermutator().getColumnCount());
    setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
  }

  @Override
  protected void populateEmptyItem(Item item) {
    item.add(new EmptyPanel("input").setVisible(false));
  }

}
