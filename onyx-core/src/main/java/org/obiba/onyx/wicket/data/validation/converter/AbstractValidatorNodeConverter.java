/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.data.validation.converter;

import org.apache.wicket.validation.IValidator;

/**
 * A helper implementation of {@code IValidatorNodeConverter}.
 */
public abstract class AbstractValidatorNodeConverter implements IValidatorNodeConverter {

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class type) {
    return type != null && type.equals(IValidator.class);
  }

}