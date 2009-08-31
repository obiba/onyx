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

import org.obiba.onyx.core.domain.Attribute;

/**
 * Participant attribute.
 */
public class ParticipantAttribute extends Attribute implements Serializable, ParticipantElement {
  //
  // Instance Variables
  //

  private static final long serialVersionUID = 1L;

  private boolean assignableAtEnrollment;

  private boolean mandatoryAtEnrollment;

  private boolean mandatoryAtReception;

  private boolean editableAtReception;

  private boolean editableAfterReception;

  private Group group;

  //
  // Methods
  //

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

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }
}