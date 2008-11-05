package org.obiba.onyx.quartz.core.wicket.layout.impl.array;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class DataListProvider<T extends Serializable> implements ISortableDataProvider {

  private static final long serialVersionUID = 1L;

  private List<T> dataList = new ArrayList<T>();

  public List<T> getDataList() {
    return dataList;
  }

  public Iterator<T> iterator(int first, int count) {
    return dataList.subList(first, first + count).iterator();
  }

  public IModel model(Object object) {
    return new Model((Serializable) object);
  }

  public int size() {
    return dataList.size();
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
