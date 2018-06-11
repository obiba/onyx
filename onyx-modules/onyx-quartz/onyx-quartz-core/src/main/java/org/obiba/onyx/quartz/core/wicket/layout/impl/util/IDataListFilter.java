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

import java.io.Serializable;

/**
 * Interface defining the filtering process.
 * @see AbstractDataListProvider
 */
public interface IDataListFilter<T> extends Serializable {

  /**
   * True if item is accepted by the filter.
   * @param item
   * @return
   */
  public boolean accept(T item);

}
