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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

/**
 * Each row is wrapped in a check box group. The columns are populated by an array of provided ICellPopulator objects.
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
public class CheckGroupView extends GroupView<CheckGroup> {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   * 
   * @param id
   * @param populators list of ICellPopulator objects that will be used to populate cell items
   * @param dataProvider data provider
   */
  public CheckGroupView(String id, List<ICellPopulator> populators, IDataProvider dataProvider) {
    this(id, (ICellPopulator[]) populators.toArray(new ICellPopulator[populators.size()]), dataProvider);
  }

  /**
   * Constructor.
   * 
   * @param id component id
   * @param populators array of ICellPopulator objects that will be used to populate cell items
   * @param dataProvider data provider
   */
  public CheckGroupView(String id, ICellPopulator[] populators, IDataProvider dataProvider) {
    super(id, populators, dataProvider);
  }

  @Override
  protected CheckGroup newGroup(String id, int index) {
    return new CheckGroup(id, new ArrayList<IModel>());
  }

  @Override
  protected CheckGroup[] newGroups(int size) {
    return new CheckGroup[size];
  }

}
