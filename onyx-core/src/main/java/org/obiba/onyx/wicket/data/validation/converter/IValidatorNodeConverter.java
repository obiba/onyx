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

import com.thoughtworks.xstream.converters.Converter;

/**
 * Interface for converter instances used by {@code DataValidatorConverter}. The {@code DataValidatorConverter} class
 * relies on implementation of this interface to do the actual work of converting an XML structure to a {@code
 * DataValidator} instance.
 */
public interface IValidatorNodeConverter extends Converter {

  /**
   * Returns the name of the node that this converter handles.
   * @return the node name handled by this converter.
   */
  public String getNodeName();
}