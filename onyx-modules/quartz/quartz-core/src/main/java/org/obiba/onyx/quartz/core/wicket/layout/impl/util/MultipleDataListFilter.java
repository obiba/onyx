/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class MultipleDataListFilter<T> extends Object implements IDataListFilter<T> {

  private static final long serialVersionUID = 1L;

  private List<IDataListFilter<T>> filters = new ArrayList<IDataListFilter<T>>();

  public MultipleDataListFilter() {
    super();
  }

  public MultipleDataListFilter<T> addFilter(IDataListFilter<T> filter) {
    filters.add(filter);
    return this;
  }

  public boolean accept(T item) {
    for(IDataListFilter<T> filter : filters) {
      if(!filter.accept(item)) return false;
    }
    return true;
  }

}
