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

import java.util.List;
import java.util.Vector;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class GroupView<T extends FormComponent> extends RowView {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(GroupView.class);

  private static final String GROUP_ID = "group";

  private Vector<T> groups;

  /**
   * Constructor.
   * 
   * @param id
   * @param populators list of ICellPopulator objects that will be used to populate cell items
   * @param dataProvider data provider
   */
  public GroupView(String id, List<ICellPopulator> populators, IDataProvider dataProvider) {
    this(id, populators.toArray(new ICellPopulator[populators.size()]), dataProvider);
  }

  /**
   * Constructor.
   * 
   * @param id component id
   * @param populators array of ICellPopulator objects that will be used to populate cell items
   * @param dataProvider data provider
   */
  public GroupView(String id, ICellPopulator[] populators, IDataProvider dataProvider) {
    super(id, populators, dataProvider);

    this.groups = new Vector<T>();
  }

  /**
   * Factory method for radio groups.
   * @param id
   * @return
   */
  protected abstract T newGroup(final String id, int index);

  /**
   * @see org.apache.wicket.markup.repeater.RefreshingView#populateItem(org.apache.wicket.markup.repeater.Item)
   */
  @SuppressWarnings("unchecked")
  protected final void populateItem(Item item) {
    T group = newGroup(GROUP_ID, item.getIndex());
    if(item.getIndex() >= groups.size()) {
      groups.setSize(item.getIndex() + 1);
    }
    groups.set(item.getIndex(), group);
    item.add(group);
    RepeatingView cells = newCells();
    group.add(cells);
    populateCells(cells, item);
  }
}
