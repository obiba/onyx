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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obiba.onyx.core.domain.AttributeValue;

/**
 * Participant attribute value.
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "participant_id", "attributeName" }) })
public class ParticipantAttributeValue extends AttributeValue {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @ManyToOne(optional = false)
  @JoinColumn(name = "participant_id")
  private Participant participant;

  //
  // Methods
  //

  public void setParticipant(Participant participant) {
    this.participant = participant;
  }

  public Participant getParticipant() {
    return participant;
  }

}
