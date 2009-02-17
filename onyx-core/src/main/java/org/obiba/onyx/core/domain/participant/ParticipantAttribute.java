/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.participant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.validation.IValidator;
import org.obiba.onyx.util.data.DataType;

/**
 * Participant attribute.
 */
public class ParticipantAttribute implements Serializable {
  //
  // Instance Variables
  //

  private static final long serialVersionUID = 1L;

  private String name;

  private DataType type;

  private Set<String> allowedValues;

  private boolean assignableAtEnrollment;

  private boolean mandatoryAtEnrollment;

  private boolean mandatoryAtReception;

  private boolean editableAtReception;

  private boolean editableAfterReception;

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

  /**
   * Sets whether the attribute is assignable at the time of enrollment.
   * 
   * @param assignableAtEnrollment whether attribute is assignable at time of enrollment
   */
  public void setAssignableAtEnrollment(boolean assignableAtEnrollment) {
    this.assignableAtEnrollment = assignableAtEnrollment;
  }

  /**
   * Indicates whether the attribute is assignable at the time of enrollment.
   * 
   * @return <code>true</code> if the attribute is assignable at the time of enrollment
   */
  public boolean isAssignableAtEnrollment() {
    return assignableAtEnrollment;
  }

  /**
   * Sets whether the attribute is mandatory at the time of enrollment.
   * 
   * @param mandatoryAtEnrollment whether attribute is mandatory at time of enrollment
   */
  public void setMandatoryAtEnrollment(boolean mandatoryAtEnrollment) {
    this.mandatoryAtEnrollment = mandatoryAtEnrollment;
  }

  /**
   * Indicates whether the attribute is mandatory at the time of enrollment.
   * 
   * @return <code>true</code> if the attribute is mandatory at the time of enrollment
   */
  public boolean isMandatoryAtEnrollment() {
    return mandatoryAtEnrollment;
  }

  /**
   * Sets whether the attributes is mandatory at the time of reception.
   * 
   * @param mandatoryAtReception whether attribute is mandatory at time of reception
   */
  public void setMandatoryAtReception(boolean mandatoryAtReception) {
    this.mandatoryAtReception = mandatoryAtReception;
  }

  /**
   * Indicates whether the attribute is mandatory at the time of reception.
   * 
   * @return <code>true</code> if the attribute is mandatory at the time of reception
   */
  public boolean isMandatoryAtReception() {
    return mandatoryAtReception;
  }

  /**
   * Sets whether the attribute is editable at the time of reception.
   * 
   * @param editableAtReception
   */
  public void setEditableAtReception(boolean editableAtReception) {
    this.editableAtReception = editableAtReception;
  }

  /**
   * Indicates whether the attribute is editable at the time of reception.
   * 
   * @return <code>true</code> if the attribute is mandatory at the time of enrollment
   */
  public boolean isEditableAtReception() {
    return editableAtReception;
  }

  /**
   * Sets whether the attribute is editable after the time of reception.
   * 
   * @param editableAfterReception whether the attribute is editable after the time of reception
   */
  public void setEditableAfterReception(boolean editableAfterReception) {
    this.editableAfterReception = editableAfterReception;
  }

  /**
   * Indicates whether the attribute is editable after the time of reception.
   * 
   * @return <code>true</code> if the attribute is editable after the time of reception.
   */
  public boolean isEditableAfterReception() {
    return editableAfterReception;
  }

  public void setValidators(List<IValidator> validators) {
    this.validators = validators;
  }
}