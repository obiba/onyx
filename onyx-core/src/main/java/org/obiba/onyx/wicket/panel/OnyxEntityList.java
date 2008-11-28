/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.panel;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.obiba.wicket.markup.html.table.EntityListTablePanel;
import org.obiba.wicket.markup.html.table.IColumnProvider;

public class OnyxEntityList<T> extends EntityListTablePanel<T> {

  private static final long serialVersionUID = 7891474467194294293L;

  public OnyxEntityList(String id, Class<T> type, IColumnProvider columns, IModel title) {
    super(id, type, columns, title);
    setAllowColumnSelection(false);
  }
  
  public OnyxEntityList(String id, T template, IColumnProvider columns, IModel title) {
    super(id, template, columns, title);
    setAllowColumnSelection(false);
  }

  public OnyxEntityList(String id, SortableDataProvider dataProvider, IColumnProvider columns, IModel title) {
    super(id, dataProvider, columns, title, 50);
    setAllowColumnSelection(false);
  }
  
}
