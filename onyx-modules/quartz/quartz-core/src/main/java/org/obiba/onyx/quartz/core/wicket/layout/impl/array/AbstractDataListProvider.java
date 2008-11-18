/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.array;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public abstract class AbstractDataListProvider<T extends Serializable> implements IDataProvider {

  private static final long serialVersionUID = 1L;

  public abstract List<T> getDataList();

  public Iterator<T> iterator(int first, int count) {
    return getDataList().subList(first, first + count).iterator();
  }

  public IModel model(Object object) {
    return new Model((Serializable) object);
  }

  public int size() {
    return getDataList().size();
  }

  public void detach() {
  }

}
