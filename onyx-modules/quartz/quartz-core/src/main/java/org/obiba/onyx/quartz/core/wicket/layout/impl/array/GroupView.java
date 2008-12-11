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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataViewBase;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.util.ArrayIteratorAdapter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Each row is wrapped in a form component group. The columns are populated by an array of provided ICellPopulator
 * objects.
 * 
 * <pre>
 *  &lt;tr wicket:id=&quot;rows&quot;&gt;
 *   &lt;wicket:container wicket:id=&quot;group&quot;&gt;
 *     &lt;td wicket:id=&quot;cells&quot;&gt;
 *       &lt;span wicket:id=&quot;cell&quot;&gt;[cell]&lt;/span&gt;
 *       &lt;/td&gt;
 *     &lt;/wicket:container&gt;
 *  &lt;/tr&gt;
 * </pre>
 * 
 * @see DataGridView
 * 
 */
public abstract class GroupView<T extends FormComponent> extends DataViewBase {

  private static final long serialVersionUID = 1L;

  private static final String RADIO_GROUP_ID = "group";

  private static final String CELL_REPEATER_ID = "cells";

  private static final String CELL_ITEM_ID = "cell";

  private ICellPopulator[] populators;

  private transient ArrayIteratorAdapter populatorsIteratorCache;

  private T[] groups;

  /**
   * Constructor.
   * 
   * @param id
   * @param populators list of ICellPopulator objects that will be used to populate cell items
   * @param dataProvider data provider
   */
  public GroupView(String id, List<ICellPopulator> populators, IDataProvider dataProvider) {
    this(id, (ICellPopulator[]) populators.toArray(new ICellPopulator[populators.size()]), dataProvider);
  }

  /**
   * Constructor.
   * 
   * @param id component id
   * @param populators array of ICellPopulator objects that will be used to populate cell items
   * @param dataProvider data provider
   */
  public GroupView(String id, ICellPopulator[] populators, IDataProvider dataProvider) {
    super(id, dataProvider);

    this.populators = Arrays.copyOf(populators, populators.length);
    this.groups = newGroups(dataProvider.size());
  }

  /**
   * Returns iterator over ICellPopulator elements in the populators array. This method caches the iterator
   * implementation in a transient member instance.
   * 
   * @return iterator over ICellPopulator elements in the populators array
   */
  @SuppressWarnings("unchecked")
  private Iterator getPopulatorsIterator() {
    if(populatorsIteratorCache == null) {
      populatorsIteratorCache = new ArrayIteratorAdapter(internalGetPopulators()) {

        protected IModel model(Object object) {
          return new Model((Serializable) object);
        }

      };
    } else {
      populatorsIteratorCache.reset();
    }
    return populatorsIteratorCache;
  }

  protected final ICellPopulator[] internalGetPopulators() {
    return populators;
  }

  protected abstract T[] newGroups(int size);

  /**
   * Factory method for radio groups.
   * @param id
   * @return
   */
  protected abstract T newGroup(final String id, int index);

  /**
   * Factory method for Item container that represents a cell.
   * 
   * @see Item
   * @see RefreshingView#newItem(String, int, IModel)
   * 
   * @param id component id for the new data item
   * @param index the index of the new data item
   * @param model the model for the new data item
   * 
   * @return DataItem created DataItem
   */
  protected Item newCellItem(final String id, int index, final IModel model) {
    return new Item(id, index, model);
  }

  protected final Item newItem(String id, int index, IModel model) {
    return newRowItem(id, index, model);
  }

  /**
   * Factory method for Item container that represents a row.
   * 
   * @see Item
   * @see RefreshingView#newItem(String, int, IModel)
   * 
   * @param id component id for the new data item
   * @param index the index of the new data item
   * @param model the model for the new data item.
   * 
   * @return DataItem created DataItem
   */
  protected Item newRowItem(final String id, int index, final IModel model) {
    return new OddEvenItem(id, index, model);
  }

  /**
   * @see org.apache.wicket.markup.repeater.data.DataViewBase#onDetach()
   */
  protected void onDetach() {
    super.onDetach();
    if(populators != null) {
      for(int i = 0; i < populators.length; i++) {
        populators[i].detach();
      }
    }
  }

  /**
   * Sets the number of items to be displayed per page
   * 
   * @param items number of items to display per page
   * 
   */
  public void setRowsPerPage(int items) {
    internalSetRowsPerPage(items);
  }

  /**
   * @return number of items displayed per page
   */
  public int getRowsPerPage() {
    return internalGetRowsPerPage();
  }

  /**
   * Returns the data provider
   * 
   * @return data provider
   */
  public IDataProvider getDataProvider() {
    return internalGetDataProvider();
  }

  public T[] getGroups() {
    return Arrays.copyOf(groups, groups.length);
  }

  public T getGroup(int index) {
    return groups[index];
  }

  /**
   * @see org.apache.wicket.markup.repeater.RefreshingView#populateItem(org.apache.wicket.markup.repeater.Item)
   */
  @SuppressWarnings("unchecked")
  protected final void populateItem(Item item) {
    T group = newGroup(RADIO_GROUP_ID, item.getIndex());
    groups[item.getIndex()] = group;
    item.add(group);
    RepeatingView cells = new RepeatingView(CELL_REPEATER_ID);
    group.add(cells);

    Iterator populators = getPopulatorsIterator();

    for(int i = 0; populators.hasNext(); i++) {
      IModel populatorModel = (IModel) populators.next();
      Item cellItem = newCellItem(cells.newChildId(), i, populatorModel);
      cells.add(cellItem);

      ICellPopulator populator = (ICellPopulator) cellItem.getModelObject();
      populator.populateItem(cellItem, CELL_ITEM_ID, item.getModel());

      if(cellItem.get("cell") == null) {
        throw new WicketRuntimeException(populator.getClass().getName() + ".populateItem() failed to add a component with id [" + CELL_ITEM_ID + "] to the provided [cellItem] object. Make sure you call add() on cellItem ( cellItem.add(new MyComponent(componentId, rowModel) )");
      }
    }

  }
}
