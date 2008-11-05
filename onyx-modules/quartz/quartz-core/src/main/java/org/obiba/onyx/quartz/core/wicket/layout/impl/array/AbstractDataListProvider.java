package org.obiba.onyx.quartz.core.wicket.layout.impl.array;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public abstract class AbstractDataListProvider<T extends Serializable> implements ISortableDataProvider {

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

  public ISortState getSortState() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setSortState(ISortState state) {
    // TODO Auto-generated method stub

  }

}
