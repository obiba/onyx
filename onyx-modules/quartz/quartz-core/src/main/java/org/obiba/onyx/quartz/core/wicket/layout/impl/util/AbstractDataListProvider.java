/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Data provider build on a templated list, with filtering and permutation facilities.
 * 
 * @param <T>
 */
public abstract class AbstractDataListProvider<T extends Serializable> implements IDataProvider<T> {

  private static final long serialVersionUID = 1L;

  protected IDataListPermutator<IModel<T>> permutator;

  public AbstractDataListProvider() {
    this(null);
  }

  public AbstractDataListProvider(IDataListPermutator<IModel<T>> permutator) {
    super();
    this.permutator = permutator;
  }

  /**
   * To be overridden if list is to be permutated.
   * @return
   */
  public IDataListPermutator<IModel<T>> getDataListPermutator() {
    return permutator;
  }

  public abstract List<IModel<T>> getDataList();

  public Iterator<T> iterator(int first, int count) {
    return Iterables.transform(getDataList().subList(first, first + count), new Function<IModel<T>, T>() {

      @Override
      public T apply(IModel<T> input) {
        return input != null ? input.getObject() : null;
      }
    }).iterator();
  }

  public abstract IModel<T> model(T object);

  public int size() {
    return getDataList().size();
  }

  public void detach() {
  }

}
