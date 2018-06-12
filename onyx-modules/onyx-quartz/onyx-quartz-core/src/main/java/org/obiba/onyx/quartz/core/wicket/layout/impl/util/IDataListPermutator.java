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
import java.util.List;

/**
 * Interface defining the list elements permutation process.
 * @see AbstractDataListProvider
 */
public interface IDataListPermutator<T> extends Serializable {

  /**
   * Permute list elements into a new list.
   * @param list
   * @return
   */
  public List<T> permute(List<T> list);

  /**
   * Get column count from the 2 dimensional representation of the permuted list.
   * @return
   */
  public int getColumnCount();

  /**
   * Get row count from the 2 dimensional representation of the permuted list.
   * @return
   */
  public int getRowCount();

}
