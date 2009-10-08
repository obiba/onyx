/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.data;

import org.apache.wicket.validation.IValidator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * Marker interface for {@code IValidator} instances that validate {@link Data} values.
 */
public interface IDataValidator extends IValidator {

  public DataType getDataType();

  /**
   * Returns the decorated validator.
   * 
   * @return decorated validator
   */
  public IValidator getValidator();
}
