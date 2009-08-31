/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.validation.IValidator;
import org.obiba.onyx.util.data.DataType;

/**
 * Attribute.
 */
public class Attribute implements Serializable {
  //
  // Instance Variables
  //

  private static final long serialVersionUID = 1L;

  private String name;

  private DataType type;

  private Set<String> allowedValues;

  private List<IValidator> validators;

  //
  // Methods
  //

  public List<IValidator> getValidators() {
    return (validators != null) ? validators : (validators = new ArrayList<IValidator>());
  }

  public void addValidators(IValidator validator) {
    getValidators().add(validator);
  }

  /**
   * Sets the name of the attribute.
   * 
   * @param name attribute name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the attribute's name.
   * 
   * @return attribute name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the attribute's data type.
   * 
   * @param type attribute data type
   */
  public void setType(DataType type) {
    this.type = type;
  }

  /**
   * Returns the attribute's data type.
   * 
   * @return attribute data type
   */
  public DataType getType() {
    return type;
  }

  /**
   * Sets the attribute's allowed values (assuming a discrete set of values is allowed).
   * 
   * @param allowedValues allowed values of attribute
   */
  public void setAllowedValues(Set<String> allowedValues) {
    if(this.allowedValues == null) {
      this.allowedValues = new LinkedHashSet<String>();
    } else {
      this.allowedValues.clear();
    }

    if(allowedValues != null) {
      this.allowedValues.addAll(allowedValues);
    }
  }

  /**
   * Returns the attribute's allowed values.
   * 
   * @return allowed values of attribute
   */
  public Set<String> getAllowedValues() {
    if(allowedValues == null) {
      allowedValues = new LinkedHashSet<String>();
    }
    return Collections.unmodifiableSet(allowedValues);
  }

  public void setValidators(List<IValidator> validators) {
    this.validators = validators;
  }

}